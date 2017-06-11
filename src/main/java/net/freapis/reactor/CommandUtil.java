package net.freapis.reactor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wuqiang on 2017/6/11.
 */
public class CommandUtil {

    public static String runShell(String shell){
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(shell);
            Process process = pb.start();
            InputStream in = process.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in,"UTF-8");
            BufferedReader bufferReader = new BufferedReader(inReader);
            String line;
            while ((line = bufferReader.readLine()) != null) {
                result.append(line);
            }
            bufferReader.close();
            inReader.close();
            in.close();
            process.waitFor();
        } catch (Exception e) {
            result.append(e.toString());
        }
        return result.toString();
    }
}
