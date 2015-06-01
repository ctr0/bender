package org.ctro.bender.xml;

import org.ctro.bender.BenderVisitor;

public class BenderXmlVisitor implements BenderVisitor {
	
	@BenderXml("/test")
	public void visitStart() {
		System.out.println("Visit test");
	}
	
	@BenderXml("test/tag1/")
	public BenderVisitor visitTag1(@BenderXml("string") String name, @BenderXml("number") int n) {
		
		System.out.println("Visit tag1 [" + name + ", " + n + "]");
		
		return new BenderVisitor() {
			
			@BenderXml("/test/tag1/tag11")
			public void visitTag11(@BenderXml("string") String name, @BenderXml("number") double n) {
				System.out.println("Visit tag11 [" + name + ", " + n + "]");
			}
			
			@BenderXml("/test/tag1/tag12")
			public void visitTag12(@BenderXml("string") String name, @BenderXml("number") float n) {
				System.out.println("Visit tag12 [" + name + ", " + n + "]");
			}
			
			@Override
			public void visitEnd() {
				System.out.println("End tag1");
			}
		};
	}

	@Override
	public void visitEnd() {
		System.out.println("End test");
	}

}
