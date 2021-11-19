package nablarch.test.tool.findbugs.eclipse;

public class UnpublishedTestForSettings {

    public void testCall() {
        Methods methods = new Methods();

        methods.publishedMethod1();
        methods.publishedMethod2();
        methods.publishedMethod3();
        methods.unpublishedMethod1();
        methods.unpublishedMethod2();
        methods.unpublishedMethod3();
        methods.methodForChangingSettings();
    }
}
