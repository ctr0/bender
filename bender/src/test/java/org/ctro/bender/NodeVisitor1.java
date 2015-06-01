package org.ctro.bender;

import org.ctro.bender.Bender;
import org.ctro.bender.BenderVisitor;

public class NodeVisitor1 implements BenderVisitor {
		
	@Bender("this")
	public void visitNode(@Bender("name") String name, @Bender("getSize()") int size) {
		System.out.println("Visiting node " + name + " with size " + size);
	}
//	
//	@Bender("childs")
//	public void visitChilds1(@Bender("length") int size) {
//		System.out.println("Visiting(1) property childs nodes with size " + size);
//	}
//	
//	@Bender("this.childs")
//	public NodeVisitor1 visitChilds2(@Bender("length") int size) {
//		System.out.println("Visiting(2) property childs nodes with size " + size);
//		return new NodeVisitor1();
//	}
//	
//	@Bender("childs[]")
//	public void visitChild1(@Bender("this.name") String name, @Bender("size") int size) {
//		System.out.println("Visiting(3) child node " + name + " with size " + size);
//	}
	
	@Bender("this.childs[]")
	public NodeVisitor1 visitChild2(@Bender("this") Node child) {
		System.out.println("Visiting(4) child node " + child);
		return new NodeVisitor1() {
			
		};
	}
	
	@Override
	public void visitEnd() {
		System.out.println("Visit end");
	}

}
