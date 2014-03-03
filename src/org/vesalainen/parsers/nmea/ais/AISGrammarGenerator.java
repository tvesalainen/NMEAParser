/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.parsers.nmea.ais;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import org.vesalainen.bcc.model.El;
import org.vesalainen.grammar.GRule;
import org.vesalainen.grammar.Grammar;
import org.vesalainen.lpg.LALRKParserGenerator;
import org.vesalainen.parser.GenClassFactory;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Rules;
import org.vesalainen.parser.annotation.Terminal;
import org.vesalainen.parser.util.InputReader;
import org.vesalainen.regex.Regex;

/**
 * @author Timo Vesalainen
 */
@GenClassname("org.vesalainen.parsers.nmea.ais.AISGrammarGeneratorImpl")
@GrammarDef
@Rules({
    @Rule(left="structures", value="structure*"),
    @Rule(left="structure", value="text"),
    @Rule(left="structure", value="title"),
    @Rule(left="structure", value="table")
})
public abstract class AISGrammarGenerator
{

    public static Grammar appendGrammar(Grammar grammar) throws FileNotFoundException
    {
        InputStream is = new FileInputStream("src\\org\\vesalainen\\parsers\\nmea\\ais\\AIVDM.txt");
        AISGrammarGenerator gen = AISGrammarGenerator.newInstance();
        grammar.addRule("messages", "(message '0*\n')+");
        for (SubareaType sat : SubareaType.values())
        {
            if (!sat.toString().startsWith("Reserved"))
            {
                grammar.addRule("shape", sat.toString());
            }
        }
        gen.parse(is, grammar);
        return grammar;
    }
    protected String lastTitle;
    private Set<String> references = new HashSet<>();

    public AISGrammarGenerator()
    {
        references.add(".Sensor report types");
        references.add(".Sensor Owner Codes");
        references.add(".Data Timeout Codes");
        references.add(".Sensor Types");
        references.add(".Vertical Reference Datum");
    }
    
    @Rule("tableTitle? start line+ end")
    protected void table(String title, List<List<String>> lines, @ParserContext("grammar") Grammar grammar)
    {
        Iterator<List<String>> iterator = lines.iterator();
        List<String> prev = null;
        while (iterator.hasNext())
        {
            List<String> next = iterator.next();
            if (next.size() == 1)
            {
                prev.set(prev.size()-1, prev.get(prev.size()-1)+" "+next.get(0).trim());
                iterator.remove();
            }
            else
            {
                prev = next;
            }
        }
        for (String ref : references)
        {
            if (title != null && ref.equals(title.trim()))
            {
                createEnum(ref, lines);
            }
        }
        createMessageRule(
                grammar, 
                title != null ? title : lastTitle, 
                lines
                );
    }
            
    @Rule("('\\|' cell?)+ '[\r\n]+'")
    protected List<String> line(List<String> cellList)
    {
        return cellList;
    }
            
    @Rule("cell '[\r\n]+'")
    protected List<String> line(String cell)
    {
        List<String> list = new ArrayList<>();
        list.add(cell);
        return list;
    }
            
    @Terminal(expression="=[^\n]*[\r\n]+")
    protected void title(String title)
    {
        lastTitle = title;
    }
    
    @Terminal(expression="\\.[^\n]*[\r\n]+")
    protected abstract String tableTitle(String title);
    
    @Terminal(expression="[^\\|\r\n]+")
    protected String cell(String text)
    {
        return text.trim();
    }
    
    @Terminal(expression="(\\[[^\n]*[\r\n]+)?\\|[=]+[\r\n]+")
    protected void start(String title)
    {
    }
    
    @Terminal(expression="\\|[=]+[\r\n]+")
    protected void end(String title)
    {
    }
    
    @Terminal(expression="[^=\\.\\[\\|].*[\n]{2}", options={Regex.Option.FIXED_ENDER})
    protected abstract void text();
    
    @ParseMethod(start="structures", size=1024)
    public abstract void parse(InputStream is, @ParserContext("grammar") Grammar grammar);
    
    public static AISGrammarGenerator newInstance()
    {
        return (AISGrammarGenerator) GenClassFactory.getGenInstance(AISGrammarGenerator.class);
    }
    public Grammar parse()
    {
        Grammar g = new Grammar(5, 50);
        String pkg = AISGrammarGenerator.class.getPackage().getName().replace('.', '/')+"/";
        InputStream is = AISGrammarGenerator.class.getClassLoader().getResourceAsStream(pkg+"AIVDM.txt");
        AISGrammarGenerator gen = AISGrammarGenerator.newInstance();
        gen.parse(is, g);
        return g;
    }
    private void createMessageRule(Grammar grammar, String title, List<List<String>> lines)
    {
        Iterator<List<String>> iterator = lines.iterator();
        List<String> header = iterator.next();
        if (check(header))
        {
            String rule = camel(title);
            System.err.println("// "+rule);
            StringBuilder rhs = new StringBuilder();
            boolean hasArray = false;
            boolean msgRule = false;
            while (iterator.hasNext())
            {
                List<String> line = iterator.next();
                String member = line.get(3);
                if (hasArray && "shape".equals(member))
                {
                    rhs.append(" shape");
                    break;
                }
                String description = line.get(2);
                String units = line.get(5);
                String t = line.get(4);
                int type = t.charAt(0);
                if (type == 'a')
                {
                    hasArray = true;
                    rhs.append(" (");
                }
                else
                {
                    Class<?> javaType = int.class;
                    int radix = 2;
                    int[] len = bounds(line.get(1));
                    switch (type)
                    {
                        case 'u':
                        case 'U':
                        case 'e':
                        case 'b':
                            if (len[0] > 31)
                            {
                                javaType = long.class;
                            }
                            break;
                        case 'i':
                        case 'I':
                            radix = -2;
                            if (len[0] > 32)
                            {
                                javaType = long.class;
                            }
                            break;
                        case 't':
                            javaType = InputReader.class;
                            break;
                        case 'x':
                            break;
                    }
                    checkReference(units);
                    if ("dac".equals(member) && "Unsigned integer".equals(units))
                    {
                        return;
                    }
                    if (units == null)
                    {
                        units = "";
                    }
                    String constant = getConstant(units);
                    if ("type".equals(member) && 
                            !(
                            "1-3".equals(constant) || 
                            "4".equals(constant) || 
                            "5".equals(constant) || 
                            "18".equals(constant) || 
                            "19".equals(constant)
                            )
                            )
                    {
                        return;
                    }
                    String expression = createExpression(len, t, units, constant);
                    if (member.isEmpty())
                    {
                        rhs.append(" `"+expression+"´");
                    }
                    else
                    {
                        String reducer = "ais"+camel(member);
                        if (t.length() > 1)
                        {
                            reducer = reducer+"_"+t;
                        }
                        String nt = member;
                        if (constant != null)
                        {
                            nt = member+constant;
                        }
                        grammar.addTerminal(
                                getReducer(reducer, javaType, AISObserver.class), 
                                nt, 
                                expression, 
                                description, 
                                0, 
                                radix);
                        rhs.append(" "+nt);
                    }
                }
                if ("type".equals(member))
                {
                    msgRule = true;
                }
            }
            if (hasArray)
            {
                rhs.append(")+");
            }
            grammar.addRule(rule, rhs.toString());
            if (msgRule)
            {
                grammar.addRule("message", rule);
            }
        }
    }

    private Set<String> generatedMethods = new HashSet<>();
    private ExecutableElement getReducer(String name, Class<?>... p) throws SecurityException
    {
        ExecutableElement method = El.getMethod(AISParser.class, name, p);
        if (method == null)
        {
            if (!generatedMethods.contains(name))
            {
                generatedMethods.add(name);
                System.err.println("protected void "+name+"("+p[0].getSimpleName()+" arg, @ParserContext(\"aisData\") AISData aisData){}");
            }
            return null;
        }
        return method;
    }
    private boolean check(List<String> header)
    {
        return (
                header.size() == 6 &&
                "Field".equals(header.get(0)) &&
                "Len".equals(header.get(1)) &&
                "Description".equals(header.get(2)) &&
                ("Member".equals(header.get(3)) || "Member/Type".equals(header.get(3))) &&
                ("T".equals(header.get(4)) || "u".equals(header.get(4))) &&
                ("Units".equals(header.get(5)) || "Encoding".equals(header.get(5)))
                );
    }

    private String createExpression(int[] len, String t, String units, String constant)
    {
        if (constant != null)
        {
            int[] bounds = bounds(constant);
            int from = bounds[0];
            int to = bounds[1];
            StringBuilder sb = new StringBuilder();
            for (int ii=from;ii<=to;ii++)
            {
                if (sb.length() > 0)
                {
                    sb.append("|");
                }
                String bin = Integer.toBinaryString(ii);
                for (int jj=bin.length();jj<len[0];jj++)
                {
                    sb.append("0");
                }
                sb.append(bin);
            }
            return sb.toString();
        }
        else
        {
            if (len[0] == len[1])
            {
                return "[01]{"+len[0]+"}";
            }
            else
            {
                return "[01]{"+len[0]+","+len[1]+"}";
            }
        }
    }

    private String getConstant(String units)
    {
        if (units.startsWith("Constant:"))
        {
            String sub = units.substring(9).trim();
            String[] ss = sub.split(" ");
            return ss[0];
        }
        if (units.startsWith("DAC = "))
        {
            return units.substring(6).trim();
        }
        if (units.startsWith("FID = "))
        {
            return units.substring(6).trim();
        }
        return null;
    }
    private int[] bounds(String s)
    {
        String[] ss = s.trim().split("-");
        if (ss.length == 1)
        {
            int l = Integer.parseInt(ss[0]);
            return new int[] {l, l};
        }
        else
        {
            int from = Integer.parseInt(ss[0]);
            int to = Integer.parseInt(ss[1]);
            return new int[] {from, to};
        }
    }
    private void checkReference(String units)
    {
        if (units != null && units.startsWith("See \"") && units.endsWith("\""))
        {
            String ref = units.substring(5, units.length()-1);
            references.add('.'+ref);
        }
    }

    private void createEnum(String title, List<List<String>> lines)
    {
        int next = 0;
        title = camel(title.substring(1));
        try
        {
            Class.forName("org.vesalainen.parsers.nmea.ais."+title);
            return;
        }
        catch (ClassNotFoundException ex)
        {
        }
        Set<String> set = new HashSet<>();
        System.err.println("public enum "+title);
        System.err.println("{");
        for (List<String> line : lines)
        {
            String code = line.get(0);
            if (code.isEmpty())
            {
                continue;
            }
            if (!"Code".equals(code) && !"Scale".equals(code))
            {
                int[] bounds = bounds(line.get(0));
                String text = line.get(1);
                for (int e=bounds[0];e<=bounds[1];e++)
                {
                    assert e == next;
                    next++;
                    String camel = camel(text);
                    if (set.contains(camel))
                    {
                        camel = camel+e;
                    }
                    set.add(camel);
                    System.err.println("/**");
                    System.err.println(" * "+text);
                    System.err.println(" */");
                    System.err.println(camel+"(\""+text+"\"),");
                }
            }
        }
        System.err.println("private String description;");
        System.err.println(title+"(String description)");
        System.err.println("{");
        System.err.println("this.description = description;");
        System.err.println("}");
        System.err.println("public String toString()");
        System.err.println("{");
        System.err.println("return description;");
        System.err.println("}");
        System.err.println("}");
    }

    private String camel(String str)
    {
        str = str.replace('=', ' ');
        str = str.trim();
        StringBuilder sb = new StringBuilder();
        String[] ss = str.split("[ \\:\\(\\)\\,/\\-\\.\\=_\"]+");
        for (String s : ss)
        {
            if (!s.isEmpty())
            {
                sb.append(upperStart(s));
            }
        }
        return sb.toString();
    }
    private String upperStart(String str)
    {
        return str.substring(0, 1).toUpperCase()+str.substring(1);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            //AISGrammarGenerator g = (AISGrammarGenerator) GenClassFactory.getGenInstance(AISGrammarGenerator.class);
            Grammar grammar = new AISGrammar();
            LALRKParserGenerator lrk = grammar.createParserGenerator("messages", false);
            lrk.printAnnotations(System.err);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
