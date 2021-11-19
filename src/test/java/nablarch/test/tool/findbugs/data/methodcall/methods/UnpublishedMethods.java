package nablarch.test.tool.findbugs.data.methodcall.methods;

public class UnpublishedMethods {

    public boolean unpublishedMethodBooleanReturn() {
        return true;
    }

    public int unpublishedMethodIntReturn() {
        return 1;
    }
    
    public void unpublishedMethodVariableParams(String... strs) {
    }
}
