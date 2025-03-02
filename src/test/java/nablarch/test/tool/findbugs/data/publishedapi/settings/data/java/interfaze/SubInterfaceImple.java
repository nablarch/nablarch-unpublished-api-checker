package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze;

public class SubInterfaceImple implements SubInterface {

    public void superPublishedInterfaceMethod() {
    }

    public void superUnpublishedMethod() {
    }

    public void subPublishedInterfaceMethod() {
    }

    public void subUnpublishedInterfaceMethod() {
    }

    // SuperInterfaceで定義されたメソッド
    public void superInterfaceMethod() {
    }

    // SuperInterfaceでデフォルトメソッド定義され、SubInterfaceでオーバーライドされたメソッド
    @Override
    public void superInterfaceDefaultMethod() {
    }

    // SubInterfaceで定義されたメソッド
    public void subInterfaceMethod() {
    }

    // 実装クラスのみで定義されたメソッド
    public void impleOnlyMethod() {
    }

}
