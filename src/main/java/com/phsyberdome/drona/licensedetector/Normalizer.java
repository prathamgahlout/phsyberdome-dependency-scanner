

package com.phsyberdome.drona.licensedetector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author Pratham Gahlout
 */
public class Normalizer {
    
    private static String URL_REGEX = "http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+";
    private static String COPYRIGHT_NOTICE_REGEX = "((?<=\\n)|.*)Copyright.+(?=\\n)|Copyright.+\\\\n";
    private static String COPYRIGHT_SYMBOLS = "[©Ⓒⓒ]";
    private static String BULLETS_NUMBERING_REGEX = "\\s(([0-9a-z]\\.\\s)+|(\\([0-9a-z]\\)\\s)+|(\\*\\s)+)|(\\s\\([i]+\\)\\s)";
    private static String COMMENTS_REGEX = "(\\/\\/|\\/\\*|#) +.*";
    private static String EXTRANEOUS_REGEX = "(?is)\\s*end of terms and conditions.*";
    private static String ADDENDIUM_EXHIBIT_REGEX = "(?s)(APPENDIX|APADDENDUM|EXHIBIT).*";
    
        
    private static Map<String,String> getVarietalWordsSpelling() {
        Map<String,String> dict = new HashMap<String, String>();
        dict.put("acknowledgment", "acknowledgement");
        dict.put("analogue", "analog");
        dict.put("analyse", "analyze");
        dict.put("artefact", "artifact");
        dict.put("authorisation", "authorization");
        dict.put("authorised", "authorized");
        dict.put("calibre", "caliber");
        dict.put("cancelled", "canceled");
        dict.put("capitalisations", "capitalizations");
        dict.put("catalogue", "catalog");
        dict.put("categorise", "categorize");
        dict.put("centre", "center");
        dict.put("emphasised", "emphasized");
        dict.put("favour", "favor");
        dict.put("favourite", "favorite");
        dict.put("fulfil", "fulfill");
        dict.put("fulfilment", "fulfillment");
        dict.put("initialise", "initialize");
        dict.put("judgment", "judgement");
        dict.put("labelling", "labeling");
        dict.put("labour", "labor");
        dict.put("licence", "license");
        dict.put("maximise", "maximize");
        dict.put("modelled", "modeled");
        dict.put("offence", "offense");
        dict.put("optimise","optimize");
        dict.put("organisation","organization");
        dict.put("organise","organize");
        dict.put("practise","practice");
        dict.put("programme","program");
        dict.put("realise","realize");
        dict.put("recognise","recognize");
        dict.put("signalling","signaling");
        dict.put("sub-license","sublicense");
        dict.put("sub license","sublicense");
        dict.put("utilisation","utilization");
        dict.put("whilst","while");
        dict.put("wilful","wilfull");
        dict.put("non-commercial","noncommercial");
        dict.put("per cent","percent");
        dict.put("owner","holder");
        
        return dict;
    }
    
    public static String normalize(String licenseText) {
        // To avoid a possibility of a non-match due to urls not being same.
        licenseText = licenseText.replaceAll(URL_REGEX, "normalized/url");
        
        // To avoid the license mismatch merely due to the existence or absence of code comment indicators placed within the license text, they are just removed.
        licenseText = licenseText.replaceAll(COMMENTS_REGEX, "");
        
        // To avoid a license mismatch merely because extraneous text that appears at the end of the terms of a license is different or missing.
        licenseText = licenseText.replaceAll(EXTRANEOUS_REGEX, "");
        licenseText = licenseText.replaceAll(ADDENDIUM_EXHIBIT_REGEX, "");
        
        // By using a default copyright symbol (c)", we can avoid the possibility of a mismatch.
        licenseText = licenseText.replaceAll(COPYRIGHT_SYMBOLS, "(C)");
        
        // To avoid a license mismatch merely because the copyright notice is different, it is not substantive and is removed.
        licenseText = licenseText.replaceAll(COPYRIGHT_NOTICE_REGEX, "");
        
        // To avoid a possibility of a non-match due to case sensitivity.
        licenseText = licenseText.toLowerCase();
        
        // To remove the license name or title present at the beginning of the license text.
        Scanner scanner = new Scanner(licenseText);
        if(scanner.hasNextLine()){
            String firstLine = scanner.nextLine();
            if(firstLine.contains("license")){
                String temp = "";
                while(scanner.hasNextLine()) {
                    temp += scanner.nextLine();
                }
                licenseText = temp;
            }
        }
        
        // To avoid the possibility of a non-match due to variations of bullets, numbers, letter, or no bullets used are simply removed.
        licenseText = licenseText.replaceAll(BULLETS_NUMBERING_REGEX, "");
        
        // To avoid the possibility of a non-match due to the same word being spelled differently.
        for (Map.Entry<String, String> entry : getVarietalWordsSpelling().entrySet()) {
            String initial = entry.getKey();
            String finalVal = entry.getValue();
            licenseText = licenseText.replace(initial, finalVal);
        }
        
        //  To avoid the possibility of a non-match due to different spacing of words, line breaks, or paragraphs.
        licenseText = String.join(" ", licenseText.split("\\P{L}+"));
        
        return licenseText;
    }

}
