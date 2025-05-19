package es.edn;

import es.edn.model.Contribuyente;
import es.edn.service.SoapService;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;

import jakarta.inject.Inject;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "checknifcli", description = "...",
        mixinStandardHelpOptions = true)
public class ChecknifcliCommand implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    @Option(names = {"-e", "--error"}, description = "...")
    String fileError;


    @Parameters(index = "0")
    String file;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(ChecknifcliCommand.class, args);
    }

    @Inject
    private SoapService soapService;


    public void run(){
        // business logic here
        if (verbose) {
            System.out.println("Hi!");
        }
        try{
            var printErr = fileError != null ? new PrintStream(fileError) : System.err;
            var filePath = Path.of(file);
            int batchSize = 1000;
            List<Contribuyente> buffer = new ArrayList<>();

            Files.lines(filePath, StandardCharsets.ISO_8859_1).forEach(line -> {
                var fields = line.split("\\|");
                if( fields.length != 2 ){
                    return;
                }
                var add = new Contribuyente(fields[0],fields[1],true,"");
                buffer.add(add);
                if (buffer.size() == batchSize) {
                    procesarBuffer(buffer, printErr);
                    buffer.clear();
                }
            });

            if (!buffer.isEmpty()) {
                procesarBuffer(buffer, printErr);
            }

        }catch( Exception e){
            e.printStackTrace();
            System.exit(-1);
        }

    }

    void procesarBuffer(List<Contribuyente> buffer, PrintStream printErr){
        soapService.checkNif(buffer)
                .stream()
                .filter(result -> !result.result())
                .forEach(invalid -> printErr.println(invalid.nif() + "|" + invalid.resultado()));
    }
}
