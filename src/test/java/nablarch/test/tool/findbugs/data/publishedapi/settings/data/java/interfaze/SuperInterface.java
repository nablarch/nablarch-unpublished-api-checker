package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze;

public interface SuperInterface {

    void superPublishedInterfaceMethod();

    void superUnpublishedMethod();

    // SubInterfaceImpleで実装されるメソッド
    void superInterfaceMethod();

    // SubInterface、SubInterfaceImpleでオーバーライドされるメソッド
    default void superInterfaceDefaultMethod() {
    }

    // SubInterface、SubInterfaceImpleでオーバーライドされないメソッド
    default void superInterfaceOnlyDefaultMethod() {
    }


}
