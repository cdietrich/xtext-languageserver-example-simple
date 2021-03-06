/*
 * generated by Xtext 2.26.0.M2
 */
package org.xtext.example.mydsl.formatting2;

import org.eclipse.xtext.formatting2.AbstractJavaFormatter;
import org.eclipse.xtext.formatting2.IFormattableDocument;
import org.xtext.example.mydsl.myDsl.Greeting;
import org.xtext.example.mydsl.myDsl.Model;

public class MyDslFormatter extends AbstractJavaFormatter {

	protected void format(Model model, IFormattableDocument doc) {
		// TODO: format HiddenRegions around keywords, attributes, cross references, etc. 
		for (Greeting greeting : model.getGreetings()) {
			doc.format(greeting);
		}
	}
	
	protected void format(Greeting model, IFormattableDocument doc) {
		doc.prepend(model, it -> it.setNewLines(2));
		if (model.getFrom() != null) {
			 doc.prepend(regionFor(model).keyword("from"), this::newLine);
			 doc.interior(
					 regionFor(model).keyword("from"),
					 regionFor(model).keyword("!"),
					 this::indent);
		}
	}
	
}
