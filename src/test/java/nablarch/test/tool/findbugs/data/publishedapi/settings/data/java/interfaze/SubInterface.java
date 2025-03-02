package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze;

public interface SubInterface extends SuperInterface {

    void subPublishedInterfaceMethod();

    void subUnpublishedInterfaceMethod();

    // SubInterfaceImpleで実装されるメソッド
    void subInterfaceMethod();

    // SubInterfaceImpleでもオーバーライドされるメソッド
    @Override
    default void superInterfaceDefaultMethod() {
    }

    // SubInterfaceImpleでオーバーライドされないメソッド
    default void subInterfaceOnlyDefaultMethod() {
    }

}
