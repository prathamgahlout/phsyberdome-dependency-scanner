/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module PluginMaven {
    requires CommonUtils;
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
    requires org.fusesource.jansi;
    requires java.xml;
    
    exports com.phsyberdome.plugin.maven;
    
    provides com.phsyberdome.common.interfaces.PluginInterface
            with com.phsyberdome.plugin.maven.PluginMaven;
}
