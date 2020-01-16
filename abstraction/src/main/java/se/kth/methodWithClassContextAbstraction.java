package se.kth;

import com.google.gson.Gson;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.*;
import spoon.reflect.path.CtPath;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class Wrapper{
    int actual_line_number;
    int base_code_line_number;
    int base_patch_number;
    int changed_patch_number;
    int line_change;

    String changed_code;
    String previous_code;
    String code_file_name;
    String message;
}

public class methodWithClassContextAbstraction {
    private static Patch fileDiff(String file1, String file2) {

        //if(revised == null || revised.fileContents == null) return MAX_LINE_COUNT;
        // gets the patch diff of th files
        List<String> file1_list = Arrays.asList(file1.split("\\r?\\n"));
        List<String> file2_list = Arrays.asList(file2.split("\\r?\\n"));
        Patch patch = DiffUtils.diff(file1_list, file2_list);

        return patch;

    }

    public static void check_diff(){
        //System.out.println("Hello World");
        String file1 = "line 1\n" +
                "line 2\n" +
                "line 3\n" +
                "line 4\n" +
                "line 5\n";
        String file2 = "line 1\n" +
                "line 2\n" +
                "line 3\n" +
                "\n"+
                "line 4\n" +
                "new line "+
                "line 5\n";
        Patch patch  = fileDiff(file1, file2);
        //System.out.println(patch);
        List<Delta> changes = patch.getDeltas();

        for (Delta change : changes) {
            // checks if the change is made in this delta;
            // DELETIONS store lines of the original file in the revised copies
            int changeStart=change.getOriginal().getPosition();
            int changeEnd=changeStart+ change.getOriginal().size();

            int revisedPos = change.getRevised().getPosition();

            System.out.println(change.getRevised()+", changeStart : "+changeStart+", Original size: "+change.getOriginal().size()
                    +", Revised Size: "+change.getRevised().size()+", Revised Pos: "+revisedPos);
        }
    }

    static boolean isOverlappingRange(int start1,int end1, int start2,int end2) {
        return Math.max(start1, start2)<= Math.min(end1, end2);
    }
    public static void add_str(String data_path, String to_add){
        try {

            String whole= "", st;
            File file = new File(data_path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((st = br.readLine()) != null)
                //System.out.println(st);
                whole = whole+st+"\n";
            whole = to_add+whole;
            PrintWriter out = new PrintWriter(data_path);
            out.print(whole);
            out.close();
        }
        catch( Exception e){
            e.printStackTrace();
        }

    }
    public static void remove_str(String data_path){
        try {
            String whole= "", st;
            File file = new File(data_path);
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((st = br.readLine()) != null)
                //System.out.println(st);
                whole = whole+st+"\n";
            PrintWriter out = new PrintWriter(data_path);
            out.print(whole.substring(2));
            out.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
    public static void walk( String path ) {
        Gson gson = new Gson();
        try {
            Wrapper [] obj = gson.fromJson(new FileReader("E:\\pantho_Tufano_processing\\unique_data.json"), Wrapper[].class);
            for(int i = 0; i <obj.length; i++) {
                //System.out.println(obj[i].changed_code);
                String data_path = path+"\\"+obj[i].code_file_name+"\\"+obj[i].base_patch_number+".java";
                String store_path = data_path.replace("codes", "method_data_processed");
                int done = generateAbstraction(new File(data_path), obj[i].actual_line_number, new File(path+"\\"+obj[i].code_file_name),store_path );
                if(done==0){
                    add_str(data_path, "/*");
                    done = generateAbstraction(new File(data_path), obj[i].actual_line_number, new File(path+"\\"+obj[i].code_file_name),store_path );
                    if(done!=0){
                        System.err.println("/* solved the parsing error");
                    }
                    else{
                        remove_str(data_path);
                        add_str(data_path, "//");
                        done = generateAbstraction(new File(data_path), obj[i].actual_line_number, new File(path+"\\"+obj[i].code_file_name),store_path );
                        if(done==2){
                            System.out.println("// solved the parcing error");
                        }
                        else{
                            remove_str(data_path);
                            System.err.println(data_path);
                            System.err.println(obj[i].actual_line_number);
                        }

                    }

                }

                System.err.println(i);
            }
            System.err.println(obj.length);
            return;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        /*
        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
                walk( f.getAbsolutePath() );
                //System.out.println( "Dir:" + f.getAbsoluteFile() );
            }
            else {
                String s = f.getAbsolutePath();
                if(s.substring(s.length()-4).equals("java")) {
                    //System.out.println("File: " + f.getAbsoluteFile());
                    //System.out.println("replaced: " + s.replace("codes", "pantho_Tufano_processing\\method_data_processed"));
                    String data_path = f.getAbsolutePath();
                    String store_path = s.replace("codes", "pantho_Tufano_processing\\method_data_processed");
                    //File file = new File(store_path);
                    //System.out.println(file.getParent());
                    //boolean bool = new File(file.getParent()).mkdir();
                    //generateAbstraction(buggy_file, buggy_line, working_dir);
                }
            }
        }
        */

    }
public static void main(String[] args) throws FileNotFoundException, IOException {
        System.out.println("COMES");
        check_diff();
        //walk("E:\\pantho_Tufano_processing\\codes");
        /*
        File buggy_file = new File("/home/toufik/Desktop/NeuralCode_experiment/before.java");
        int buggy_line = Integer.parseInt("7");
        File working_dir = new File("/home/toufik/Desktop/NeuralCode_experiment/");

        if(!buggy_file.exists() || buggy_file.isDirectory()) {
                System.err.println( "buggy  does not exists or is a directory.");
                System.exit(1);
        }

        if(!working_dir.exists() || !working_dir.isDirectory()) {
                System.err.println( "working does not exists or is not a directory.");
                System.exit(1);
        }
        walk("E:\\codes");
        //generateAbstraction(buggy_file, buggy_line, working_dir);
        */
        System.exit(0);
}

public static int generateAbstraction(File buggy_file, int buggy_line, File working_dir, String store_path){
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setCommentEnabled(true);
        launcher.addInputResource(buggy_file.toString());
        try {
                launcher.buildModel();
        } catch (Exception e) {
                System.err.println("Failed to build spoon model for " + buggy_file.getAbsolutePath());
                //e.printStackTrace();
                return 0;
                //System.exit(1);
        }


        CtModel model = launcher.getModel();
        CtMethod topLevelmethod = null;
        CtElement buggy_ctElement = null;
        CtElement tmp_ctElement = null;
        CtPath buggy_ctElement_ctPath = null;
        try {
            for (CtType<?> ctType : model.getAllTypes()) {
                for (Iterator<CtElement> desIter = ctType.descendantIterator(); desIter.hasNext(); ) {
                    tmp_ctElement = desIter.next();
                    try {
                        if (tmp_ctElement.getPosition().getLine() == buggy_line && !(tmp_ctElement instanceof CtComment)) {
                            buggy_ctElement = tmp_ctElement;
                            buggy_ctElement_ctPath = tmp_ctElement.getPath();
                            //buggy_ctElement.addComment(buggy_ctElement.getFactory().Code().createComment("ONLY FOR TOKENIZATION, BUGGY LINE BELOW", CtComment.CommentType.INLINE));
                            break;
                        } else if (tmp_ctElement.getPosition().getLine() == buggy_line && (tmp_ctElement instanceof CtComment)) {
                            buggy_ctElement = tmp_ctElement;
                            buggy_ctElement_ctPath = tmp_ctElement.getPath();
                            //buggy_ctElement = buggy_ctElement.getFactory().Code().createComment("ONLY FOR TOKENIZATION, BUGGY LINE BELOW", CtComment.CommentType.INLINE);
                            //(CtComment)buggy_ctElement.setContent("ONLY FOR TOKENIZATION, BUGGY LINE BELOW");
                            //(CtComment)buggy_ctElement.setCommentType(CtComment.CommentType.INLINE);
                            break;
                        }
                    } catch (java.lang.UnsupportedOperationException e) {
                        continue;
                    }
                }
                if (buggy_ctElement != null) {
                    break;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if(buggy_ctElement == null){
          System.err.println("Could not find CtElement at line " + buggy_line +" in " + buggy_file);
            return 1;
        }
        topLevelmethod = getTopLevelMethod(buggy_ctElement);
        if(topLevelmethod == null) {
            System.err.println(" topLevelmethod == null so Buggy ctElement is not inside of CtMethod");
            return 1;
            //System.exit(1);
        }
        //System.out.println("comes");

        //System.out.println(topLevelmethod);
        File file = new File(store_path);
        try{
            file.createNewFile();
            PrintStream out = new PrintStream(new FileOutputStream(store_path));
            System.setOut(out);
            System.out.println((topLevelmethod));
            System.err.println("Storing in "+ store_path);
            return 2;
        }catch (Exception e) {
            return 1;
            //e.printStackTrace();
        }
        /*
        // Remove method body except diff
        List<CtMethod> all_methods = model.getElements(new TypeFilter(CtMethod.class));
        for(CtMethod method : all_methods) {
			if(topLevelmethod != null) {
                if(!getTopLevelMethod(method).getSignature().equals(topLevelmethod.getSignature())) {
                        method.setBody(new CtBlockImpl());
                }
			}
			else {
				method.setBody(new CtBlockImpl());
			}
        }

        // Remove constructor body
        List<CtConstructor> all_constructors = model.getElements(new TypeFilter(CtConstructor.class));
        for(CtConstructor constructor : all_constructors) {
                constructor.setBody(new CtBlockImpl());
        }

        // Remove static initializer
        List<CtAnonymousExecutable> all_anonymousExecutables = model.getElements(new TypeFilter(CtAnonymousExecutable.class));
        for(CtAnonymousExecutable anonymousExecutable : all_anonymousExecutables) {
                anonymousExecutable.delete();
        }

        for(CtType<?> ctType : model.getAllTypes()) {
            ctType.updateAllParentsBelow();
			if(topLevelmethod != null) {
				if(ctType.getQualifiedName().equals(topLevelmethod.getParent(CtType.class).getTopLevelType().getQualifiedName())){
				  try{
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(working_dir, buggy_file.getName().split(".java")[0]+"_abstract.java")));
					writer.write(ctType.toString()+'\n');
					writer.close();
					break;
				  }catch(java.io.IOException e){
					System.err.println("Error when writing abstraction to file");
					System.exit(1);
				  }
				}
			}
			else {
				try{
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File(working_dir, buggy_file.getName().split(".java")[0]+"_abstract.java")));
					writer.write(ctType.toString()+'\n');
					writer.close();
					break;
				  }catch(java.io.IOException e){
					System.err.println("Error when writing abstraction to file");
					System.exit(1);
				  }
			}
        }
         */
}
public static CtMethod getTopLevelMethod(CtMethod ctMethod) {
        CtMethod topLevelMethod = ctMethod;
        while(topLevelMethod.getParent(CtMethod.class) != null) {
                topLevelMethod = topLevelMethod.getParent(CtMethod.class);
        }
        return topLevelMethod;
}

public static CtMethod getTopLevelMethod(CtElement ctElement) {
        CtMethod topLevelMethod = null;
        topLevelMethod = ctElement.getParent(CtMethod.class);
        while(topLevelMethod != null && topLevelMethod.getParent(CtMethod.class) != null) {
                topLevelMethod = topLevelMethod.getParent(CtMethod.class);
        }
        return topLevelMethod;
}
}
