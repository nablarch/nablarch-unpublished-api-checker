package nablarch.test.tool.findbugs.data.methodcall.inherit.method;

public class ClassC extends ClassB {

    public void methodC() {

        // オーバーライドあり
        publishedPublishedOverriddenMethod();
        publishedUnpublishedOverriddenMethod();
        unpublishedPublishedOverriddenMethod();
        unpublishedUnpublishedOverriddenMethod();

        // オーバーライドなし
        publishedMethodA();
        unpublishedMethodA();

    }
}
