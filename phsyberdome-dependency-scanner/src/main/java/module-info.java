/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/module-info.java to edit this template
 */

module PhsyberdomeScaCli {
    requires org.fusesource.jansi;
    requires httpclient;
    requires httpcore;
    requires commons.logging;
    requires java.xml;
    requires java.logging;
    requires LicenseDetector;
    requires CommonUtils;
    requires PluginMaven;
    requires PluginNpm;
    
    uses com.phsyberdome.plugin.npm.PluginNpm;
    uses com.phsyberdome.plugin.maven.PluginMaven;
}
