
package document.processing;

/**
 *
 * @author umang_borad
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Document_reader {

    private static final String[] SPECIAL = new String[]{"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "=", "+", "[", "{", "]", "}", "\\\\", "|", ";", ":", "'", "\"", ",", "<", ".", ">", "/", "?", "~", "`"};
    private static final char[] SPECIAL_CHARS = new char[]{'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', '{', ']', '}', '|', ';', ':', '\'', ',', '<', '.', '>', '/', '?', '~', '`', '±', '°', '“', '®', '?'};
    private static List<String> token;
    private static int num_of_tokens;
    
    private static boolean isSpecial(char val) {
        return isSpecial(String.valueOf(val));
    }

    /**
     * @param val
     * @return {@link Boolean}
     */
    private static boolean isSpecial(String val) {
        for (String tmp : SPECIAL) {
            if (val.equals(tmp)) {
                return true;
            }
        }
        return false;
    }

    

    
 
    private static List<String> process(String input) throws BusinessException {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        char[] arr = input.toCharArray();
        for (int i = 0; i < arr.length; i++) {

            char prior = (i - 1 > 0) ? arr[i - 1] : ' ';
            char current = arr[i];
            char next = (i + 1 < arr.length) ? arr[i + 1] : ' ';

			// extract acronyms
            // this will actually extract acronyms of any length
            // once it detects this pattern a.b.c 
            // it's a greedy lexer that breaks at ' '
            if (Character.isLetter(current) && '.' == next) {

				// Pattern-1	= U.S.A 	(5 chars)
                // Pattern-2	= U.S.A. 	(6 chars)
                if (i + 5 < input.length()) {

                    // Pattern-1
                    if (Character.isLetter(arr[i])
                            && '.' == arr[i + 1]
                            && Character.isLetter(arr[i + 2])
                            && '.' == arr[i + 3]
                            && Character.isLetter(arr[i + 4])) {

                        for (; i < arr.length && arr[i] != ' '; i++) {
                            sb.append(arr[i]);
                        }

                        // check for Pattern-2 (trailing '.')
                        if (i + 1 < input.length()
                                && '.' == arr[i + 1]) {
                            sb.append(arr[i++]);
                        }

                        addToken(tokens, sb);
                        sb = new StringBuilder();
                        continue;
                    }
                }
            }

            if ('w' == current && '/' == next) {
                sb.append(current);
                sb.append(next);
                addToken(tokens, sb);
                sb = new StringBuilder();
                i += 1;
                continue;
            }

            // extract URLs
            if ('h' == current && 't' == next) {
                if (i + 7 < input.length()
                        && "http://".equals(input.substring(i, i + 7))) {

                    for (; i < arr.length && arr[i] != ' '; i++) {
                        sb.append(arr[i]);
                    }

                    addToken(tokens, sb);
                    sb = new StringBuilder();
                    continue;
                }
            }

			// extract windows drive letter paths
            // c:/ or c:\
            if (Character.isLetter(current) && ':' == next) {
                if (i + 2 < input.length()
                        && (arr[i + 2] == '\\'
                        || arr[i + 2] == '/')) {
                    sb.append(current);
                    sb.append(next);
                    sb.append(arr[i + 2]);
                    i += 2;
                    continue;
                }
            }

			// keep numbers together when separated by a period
            // "4.0" should not be tokenized as { "4", ".", "0" }
            if (Character.isDigit(current) && '.' == next) {
                if (i + 2 < input.length()
                        && Character.isDigit(arr[i + 2])) {
                    sb.append(current);
                    sb.append(next);
                    sb.append(arr[i + 2]);
                    i += 2;
                    continue;
                }
            }

			// keep alpha characters separated by hyphens together
            // "b-node" should not be tokenized as { "b", "-", "node" }
            if (Character.isLetter(current) && '-' == next) {
                if (i + 2 < input.length()
                        && Character.isLetter(arr[i + 2])) {
                    sb.append(current);
                    sb.append(next);
                    sb.append(arr[i + 2]);
                    i += 2;
                    continue;
                }
            }

			// TODO: need a greedy look-ahead to 
            // avoid splitting this into multiple tokens 
            // "redbook@vnet.ibm.com" currently is 
            // tokenized as { "redbook@vnet", ".", "ibm", ".", "com" }
            // need to greedily lex all tokens up to the space
            // once the space is found, see if the last 4 chars are '.com' 
            // if so, then take the entire segment as a single token
            // don't separate tokens concatenated with an underscore
            // eg. "ws_srv01" is a single token, not { "ws", "_", "srv01" }
            if (Character.isLetter(current) && '_' == next) {
                if (i + 2 < input.length()
                        && Character.isLetter(arr[i + 2])) {
                    sb.append(current);
                    sb.append(next);
                    i++;
                    continue;
                }
            }

            // extract twitter channels
            if (('#' == current
                    || '@' == current)
                    && ' ' != next
                    && !Document_reader.isSpecial(next)) {
                sb.append(current);
                continue;
            }

            // keep tokens like tcp/ip and os/2 and system/z together
            if (' ' != current && '/' == next) {
                sb.append(current);
                sb.append(next);
                i++;
                continue;
            }

            if (' ' == current) {
                addToken(tokens, sb);
                sb = new StringBuilder();
                continue;
            }

			// don't tokenize on <word>'s or <words>'
            // but do tokenize on '<words>
            if ('\'' == current) {
                if (' ' == prior) {
                    addToken(tokens, "'");
                } else {
                    sb.append(current);
                }

                continue;
            }

            if (Document_reader.isSpecial(current)) {
                addToken(tokens, sb);
                addToken(tokens, String.valueOf(current));
                sb = new StringBuilder();
                continue;
            }

            sb.append(current);
        }

        if (0 != sb.length()) {
            addToken(tokens, sb);
        }

        return tokens;
    }

    protected static void addToken(List<String> tokens, String text) {
        if (!text.isEmpty()) {
            String[] a;                 //Changes made in here
            if(text.contains("\n")) {
                a=text.split("\n");
                for(int q=0;q<a.length;q++)
                {   if(a[q].contains("\r"))
                        a[q]=a[q].replace("\r", "");
                    tokens.add(a[q].toUpperCase());}
            } else tokens.add(text.toUpperCase());
        }
    }

    protected static void addToken(List<String> tokens, StringBuilder buffer) {
        if (null != buffer && 0 != buffer.length()) {
            addToken(tokens, buffer.toString().trim());
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @throws BusinessException
     */
    @SuppressWarnings("unchecked")
    public static  List<String> tokenizeDocument(File file) throws IOException, BusinessException {
 
        String ext = FilenameUtils.getExtension(file.getName());
        
        if("txt".equals(ext))
        {
            try (Scanner s = new Scanner(file)) {
                String str=new String();
                while(s.hasNext())
                {
                    str= str.concat(s.next()+" ");
                }
                
                token=process(str);


                num_of_tokens=token.size();
                return token;
            }
        }
        else if("pdf".equals(ext))
        {
            try (PDDocument document = PDDocument.load(file)) {
                PDFTextStripper textstripper = new PDFTextStripper();
                String str = textstripper.getText(document);
                token=process(str);
                


                document.close();
                num_of_tokens=token.size();
                return token;
            }
        
        }
        else
        {
                
        return null;
        }
    }

}
