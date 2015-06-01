package org.ctro.bender;

public class Node {
	
	private String name;
	private int size;
	
	private Node[] childs;
	
	public Node() {
		this("root", 2);
		childs[0] = new Node("child0", 0);
		childs[1] = new Node("child1", 0);
	}

	public Node(String name, int size) {
		this.name = name;
		this.size = size;
		this.childs = new Node[size];
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Node[] getChilds() {
		return childs;
	}

	public void setChilds(Node[] childs) {
		this.childs = childs;
	}
	
}
