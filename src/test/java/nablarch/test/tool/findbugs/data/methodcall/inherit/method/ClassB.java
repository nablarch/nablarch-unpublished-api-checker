package nablarch.test.tool.findbugs.data.methodcall.inherit.method;

public class ClassB extends ClassA {

    @Override
    public void publishedPublishedOverriddenMethod() {
        super.publishedPublishedOverriddenMethod();
    }

    @Override
    public void publishedUnpublishedOverriddenMethod() {
        super.publishedUnpublishedOverriddenMethod();
    }

    @Override
    public void unpublishedUnpublishedOverriddenMethod() {
        super.unpublishedUnpublishedOverriddenMethod();
    }

    @Override
    public void unpublishedPublishedOverriddenMethod() {
        super.unpublishedPublishedOverriddenMethod();
    }
}
