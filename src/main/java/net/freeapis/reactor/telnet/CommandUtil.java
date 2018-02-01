package net.freeapis.reactor.telnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by wuqiang on 2017/6/11.
 */
public class CommandUtil {

    public static String runShell(File workspace, String... shellElements) throws Exception{
        ProcessBuilder pb;
        Process process;

        StringBuilder result = new StringBuilder();
        InputStream in = null;
        InputStreamReader inReader = null;
        BufferedReader bufferReader = null;

        StringBuilder errorMsg = new StringBuilder();
        InputStream error = null;
        InputStreamReader errorReader = null;
        BufferedReader errorBufferReader = null;

        try {
            pb = new ProcessBuilder(shellElements).directory(workspace);
            process = pb.start();

            in = process.getInputStream();
            inReader = new InputStreamReader(in,"UTF-8");
            bufferReader = new BufferedReader(inReader);
            String line = null;

            error = process.getErrorStream();
            errorReader = new InputStreamReader(error);
            errorBufferReader = new BufferedReader(errorReader);
            String errorLine = null;

            while ((line = bufferReader.readLine()) != null
                    || (errorLine = errorBufferReader.readLine()) != null) {
                if(line != null)
                    result.append(line);
                if(errorLine != null)
                    errorMsg.append(errorLine);
            }
            process.waitFor();

            if(!errorMsg.toString().isEmpty())
                throw new RuntimeException(errorMsg.toString());
            return result.toString();
        } finally {
            if(bufferReader != null) bufferReader.close();
            if(inReader != null) inReader.close();
            if(in != null) in.close();
        }
    }

    public static void main(String[] args) throws Exception{
        /*String runResult = CommandUtil.runShell(
                new File("/home/wuqiang/style-swap"),
                "th",
                "style-swap.lua",
                "--content",
                "images/content/huaban.jpg",
                "--style",
                "images/style/starry_night.jpg",
                "--save",
                "output"
        );*/

        /*String fastNeuralResult = CommandUtil.runShell(
                new File("/root/chainer-fast-neuralstyle"),
                "python",
                "generate.py",
                "sample_images/night.jpg",
                "--model",
                "models/starrynight.model",
                "--out",
                "night_stylized.jpg",
                "--gpu",
                "0"
        );*/
        CommandUtil.runShell(null,"tasklist");
    }
}
