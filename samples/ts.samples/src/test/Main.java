package test;

import java.io.File;
import java.io.IOException;

import ts.TypeScriptException;
import ts.client.ITypeScriptServiceClient;
import ts.client.TypeScriptServiceClient;
import ts.client.completions.CompletionInfo;
import ts.client.completions.ICompletionEntry;
import ts.client.completions.ICompletionInfo;
import ts.client.definition.DefinitionsInfo;
import ts.utils.FileUtils;

public class Main {

	public static void main(String[] args) throws InterruptedException, TypeScriptException, IOException {

		File projectDir = new File("./samples");
		// sample2.ts has the following content: 
		// var s = "";s.
		File sampleFile = new File(projectDir, "sample.ts");
		String fileName = FileUtils.getPath(sampleFile);
		
		// Create TypeScript client
		ITypeScriptServiceClient client = new TypeScriptServiceClient(projectDir, new File("../../core/ts.repository/node_modules/typescript/bin/tsserver"), null);
		
		// Open "sample2.ts" in an editor
		client.openFile(fileName, null);
		
		// Do completion after the last dot of "s" variable which is a String (charAt, ....)
		CompletionInfo completionInfo = new CompletionInfo(null);
		client.completions(fileName, 1, 14, null, completionInfo);
		display(completionInfo);

		// Update the editor content to set s as number
		client.updateFile(fileName, "var s = 1;s.");
				
		// Do completion after the last dot of "s" variable which is a Number (toExponential, ....)
		completionInfo = new CompletionInfo(null);
		client.completions(fileName, 1, 14, null, completionInfo);
		display(completionInfo);

		DefinitionsInfo definitionInfo = new DefinitionsInfo();
		client.definition(fileName, 1,12, definitionInfo);
		display(definitionInfo);
		
		client.join();
		client.dispose();
		
	}

	private static void display(DefinitionsInfo definitionInfo) {
		// TODO Auto-generated method stub
		
	}

	private static void display(ICompletionInfo completionInfo) {
		System.out.println("getCompletionsAtLineOffset:");
		ICompletionEntry[] entries = completionInfo.getEntries();
		for (ICompletionEntry entry : entries) {
			System.out.println(entry.getName());	
		}
	}
}
