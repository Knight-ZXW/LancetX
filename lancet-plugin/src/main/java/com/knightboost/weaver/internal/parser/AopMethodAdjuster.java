package com.knightboost.weaver.internal.parser;

import com.knightboost.weaver.api.Origin;
import com.knightboost.weaver.api.This;
import com.knightboost.weaver.internal.util.PrimitiveUtil;
import com.knightboost.weaver.internal.util.TypeUtils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 *
 * Weaver 函数 内 字节码指令修正
 * Created by Knight-ZXW
 */
public class AopMethodAdjuster {



    public static  int OP_CALL = 225;
    public static  int OP_THIS_GET_FIELD =226;
    public static  int OP_THIS_PUT_FIELD = 227;


    public static final String JAVA_LANG_OBJECT = "java/lang/Object";


    private final boolean allowField;
    private final String sourceClass;
    private final MethodNode methodNode;


    private final CallReplacer CALL_REPLACER;
    private final ThisReplacer THIS_REPLACER = new ThisReplacer();


    public AopMethodAdjuster(boolean allowField, String sourceClass, MethodNode methodNode) {
        this.allowField = allowField;
        this.sourceClass = sourceClass;
        this.methodNode = methodNode;
        this.CALL_REPLACER = new CallReplacer(methodNode.desc);
        init();
    }

    private void init() {
        String desc = methodNode.desc;

        // update max locals
        int size = Type.getArgumentsAndReturnSizes(desc);
        if (TypeUtils.isStatic(methodNode.access)) {
            size -= 4;
        }
        size = Math.max(size & 3, size >> 2);
        methodNode.maxStack = Math.max(size, methodNode.maxStack);
    }

    public void adjust() {
        AbstractInsnNode element = methodNode.instructions.getFirst();
        while (element != null) {
            if (element instanceof MethodInsnNode) { //MethodInsnNode
                //遍历 waver函数的 函数调用类型指令
                element = transform((MethodInsnNode) element);
            }
            element = element.getNext();
        }
    }

    private AbstractInsnNode transform(MethodInsnNode node) {
        String owner = node.owner;
        String name = node.name;
        NodeReplacer replacer = NodeReplacer.IDENTITY;
        if (owner.equals(Origin.CLASS_NAME)) {
            if (name.startsWith("call")) {
                replacer = this.CALL_REPLACER;
            }
        } else if (owner.equals(This.CLASS_NAME)) {
            replacer = THIS_REPLACER;
        }
        return replacer.replace(node);
    }

    private interface NodeReplacer {

        NodeReplacer IDENTITY = node -> node;

        AbstractInsnNode replace(MethodInsnNode node);
    }

    private class CallReplacer implements NodeReplacer {

        private static final int VOID = 1;
        private static final int REFERENCE = 2;
        private static final int PRIMITIVE = 3;
        private int type;
        private String returnType;

        CallReplacer(String desc) {
            String retDesc = desc.substring(desc.lastIndexOf(')') + 1);
            if (retDesc.equals("V")) {// void
                type = VOID;
            } else if (retDesc.endsWith(";") || retDesc.charAt(0) == '[') { // object or array
                type = REFERENCE;
                if (retDesc.charAt(0) != '[' && retDesc.endsWith(";")) { //convert to internal name
                    retDesc = retDesc.substring(1, retDesc.length() - 1);
                }
                returnType = retDesc;
            } else { //primitive
                type = PRIMITIVE;
                returnType = PrimitiveUtil.box(retDesc);
            }
        }

        @Override
        public AbstractInsnNode replace(MethodInsnNode node) {
            checkReturnType(node);

            node.setOpcode(OP_CALL);
            if (type != VOID && !returnType.equals(JAVA_LANG_OBJECT)) {
                //如果函数的返回值类型不是Object，则调用后必须立即做类型转换 like
                //即下一个指令必须为
                checkCast(node.getNext());
                //移除 cast 操作
                methodNode.instructions.remove(node.getNext());
            }
            if (type == PRIMITIVE) {
                // 不允许做自动拆箱，自动拆箱动作由 adjust来完成
                checkUnbox(node.getNext());
                methodNode.instructions.remove(node.getNext());
            }
            return node;
        }

        private void checkReturnType(MethodInsnNode node) {
            boolean has = !node.name.startsWith("callVoid");
            boolean hasInDesc = type != VOID;
            if (has != hasInDesc) {
                illegalState("原函数的返回类型不为void，请选择合适的代理函数调用");
            }
        }

        private void checkCast(AbstractInsnNode insnNode) {
            if (!(insnNode instanceof TypeInsnNode)) {
                illegalState("Returned Object type should be cast to origin type immediately.");
            }
            TypeInsnNode typeInsnNode = (TypeInsnNode) insnNode;
            if (typeInsnNode.getOpcode() != Opcodes.CHECKCAST) {
                illegalState("Returned Object type should be cast to origin type immediately.");
            }
            if (!typeInsnNode.desc.equals(returnType)) {
                illegalState("Casted type is expected to be " + returnType + " , but is " + typeInsnNode.desc);
            }
        }

        private void checkUnbox(AbstractInsnNode insnNode) {
            if (!(insnNode instanceof MethodInsnNode)) {
                illegalState("Please don't unbox by your self.");
            }
            MethodInsnNode methodInsnNode = (MethodInsnNode) insnNode;
            if (!methodInsnNode.owner.equals(returnType)) {
                illegalState("Please don't unbox by your self.");
            }
            if (!methodInsnNode.name.equals(PrimitiveUtil.unboxMethod(returnType))) {
                illegalState("Please don't unbox by your self.");
            }
        }
    }

    private class ThisReplacer implements NodeReplacer {

        @Override
        public AbstractInsnNode replace(MethodInsnNode node) {
            if (TypeUtils.isStatic(methodNode.access)) {
                illegalState("static method shouldn't call This's function");
            }
            switch (node.name) {
                case "get":
                    VarInsnNode varInsnNode = new VarInsnNode(Opcodes.ALOAD, 0);
                    //TODO: Should us remove checkcast ?
                    // getType(node.getNext());
                    methodNode.instructions.set(node, varInsnNode);
                    return varInsnNode;
                case "getField":
                    checkAllow(node.name);
                    node.setOpcode(OP_THIS_GET_FIELD);
                    node.name = getFieldName(node.getPrevious());
                    break;

                case "putField":
                    checkAllow(node.name);
                    node.setOpcode(OP_THIS_PUT_FIELD);
                    node.name = getFieldName(node.getPrevious());
                    break;
            }
            return node;
        }

        private void checkAllow(String name) {
            if (!allowField) {
                illegalState("This." + name + " only allow in @Insert");
            }
        }

        private void removeBox(AbstractInsnNode previous) {
            if (previous instanceof MethodInsnNode) {
                MethodInsnNode node = (MethodInsnNode) previous;
                if (node.getOpcode() == Opcodes.INVOKESTATIC
                        && node.name.equals("valueOf")
                        && PrimitiveUtil.boxedTypes().contains(node.owner)) {
                    methodNode.instructions.remove(previous);
                }
            }
        }

        private String getType(AbstractInsnNode next) {
            if (next instanceof TypeInsnNode && next.getOpcode() == Opcodes.CHECKCAST) {
                String desc = ((TypeInsnNode) next).desc;
                if (next.getNext() instanceof MethodInsnNode) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) next.getNext();
                    if (methodInsnNode.owner.equals(desc) && methodInsnNode.name.equals(PrimitiveUtil.unboxMethod(desc))) {
                        methodNode.instructions.remove(methodInsnNode);
                    }
                }
                methodNode.instructions.remove(next);
                return desc;
            } else {
                return JAVA_LANG_OBJECT;
            }
        }

        private String getFieldName(AbstractInsnNode node) {
            if (!(node instanceof LdcInsnNode)) {
                illegalState("only accept constant string as field name");
            }
            LdcInsnNode ldc = (LdcInsnNode) node;
            if (ldc.cst == null || !(ldc.cst instanceof String)) {
                illegalState("only accept constant string as field name");
            }
            String val = (String) ldc.cst;
            methodNode.instructions.remove(node);
            return val;
        }
    }


    private void illegalState(String msg) {
        throw new IllegalStateException("error in " + sourceClass + "." + methodNode.name + ": " + msg);
    }
}
