package nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.enumz;

import nablarch.test.tool.findbugs.data.publishedapi.settings.data.java.interfaze.InterfaceFor1Interface;

public enum TestImpleEnum implements InterfaceFor1Interface {
    APPLE("apple"),
    BANANA("banana");

    private final String name;

    TestImpleEnum(String name) {
        this.name = name;
    }

    @Override
    public void interfaceMethod() {

    }

    @Override
    public void interfaceDefaultMethod() {
        InterfaceFor1Interface.super.interfaceDefaultMethod();
    }

    public void impleOnlyMethod() {

    }
}
