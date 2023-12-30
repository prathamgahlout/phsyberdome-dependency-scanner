

package com.phsyberdome.drona;

import com.phsyberdome.drona.Utils.CommandBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Pratham Gahlout
 */
public class SysRunner {

    public static String run(String cmd) {
        try {
            String[] command = CommandBuilder.buildCommandWith(cmd);
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader outputReader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
            );

            BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream())
            );

            String output = "";
            String line = null;
            while((line  = outputReader.readLine()) != null) {
                output = output.concat(line + "\n");
            }

            outputReader.close();
            errorReader.close();

            return output;
        } catch(IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }
   
    
}
