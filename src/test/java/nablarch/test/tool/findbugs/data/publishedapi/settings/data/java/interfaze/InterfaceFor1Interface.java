package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze;

public interface InterfaceFor1Interface {

    void interfaceMethod();

    default void interfaceDefaultMethod() {
    }

    default void interfaceOnlyDefaultMethod() {
    }
}
