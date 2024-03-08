
module PluginCargo {
    requires CommonUtils;
    requires org.fusesource.jansi;
    requires org.eclipse.jgit;
    requires jsch;
    requires JavaEWAH;
    requires httpclient;
    requires httpcore;
    requires commons.logging;
    requires commons.codec;
    requires org.apache.commons.io;
    requires org.apache.commons.compress;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires semver4j;
    requires toml4j;
    requires gson;
    
    exports com.phsyberdome.plugin.cargo;
    
    provides com.phsyberdome.common.interfaces.PluginInterface
            with com.phsyberdome.plugin.cargo.PluginCargo;
}
