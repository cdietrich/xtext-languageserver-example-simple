package org.xtext.example.mydsl.ide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.xtext.ide.server.Document;
import org.eclipse.xtext.ide.server.ILanguageServerAccess;
import org.eclipse.xtext.ide.server.codeActions.ICodeActionService2;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.xtext.example.mydsl.validation.MyDslValidator;

import com.google.common.collect.Lists;

public class MyDslCodeActionService implements ICodeActionService2 {
	protected List<TextEdit> addTextEdit(WorkspaceEdit edit, URI uri, TextEdit... textEdit) {
		return edit.getChanges().put(uri.toString(), Arrays.asList(textEdit));
	}

	@Override
	public List<Either<Command, CodeAction>> getCodeActions(ICodeActionService2.Options options) {
		ArrayList<CodeAction> result = new ArrayList<>();
		
		for (Diagnostic d : options.getCodeActionParams().getContext().getDiagnostics()) {
			if (MyDslValidator.INVALID_NAME.equals(d.getCode().get())) {
				result.addAll(options.getLanguageServerAccess()
						.doSyncRead(options.getURI(), (ILanguageServerAccess.Context context) -> {
							options.setDocument(context.getDocument());
							options.setResource(context.getResource());
							return getCodeActions(options, d);
						}));
			}
		}
		
		
		return Lists.transform(result, (CodeAction it) -> {
			return Either.forRight(it);
		});
	}

	private List<CodeAction> getCodeActions(Options options, Diagnostic d) {
		ArrayList<CodeAction> result = new ArrayList<>();
		Resource resource = options.getResource();
		Document document = options.getDocument();
		String text = document.getSubstring(d.getRange());
		CodeAction codeAction = new CodeAction();
		codeAction.setKind(CodeActionKind.QuickFix);
		codeAction.setTitle("Capitalize Name");
		codeAction.setDiagnostics(Lists.newArrayList(d));
		WorkspaceEdit workspaceEdit = new WorkspaceEdit();
		TextEdit textEdit = new TextEdit();
		textEdit.setRange(d.getRange());
		textEdit.setNewText(StringExtensions.toFirstUpper(text));
		addTextEdit(workspaceEdit, resource.getURI(), textEdit);
		codeAction.setEdit(workspaceEdit);
		result.add(codeAction);
		return result;
	}
}
