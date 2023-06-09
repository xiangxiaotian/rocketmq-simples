package com.example.rocketmq.test;

import java.util.ArrayList;
import java.util.List;

public class Test {

    static class Book {

        Integer id;

        String code;

        Integer parentId;

        Integer price;

        boolean isBook;

        public Book (Integer id, String code, Integer parentId, Integer price, boolean isBook) {
            this.id = id;
            this.code = code;
            this.parentId = parentId;
            this.price = price;
            this.isBook = isBook;
        }

    }

    static class TreeNode {

        List<TreeNode> childrenList;

        Book value;

        public TreeNode (Book value, List<TreeNode> childrenList) {
            this.value = value;
            this.childrenList = childrenList;
        }

    }

    public static TreeNode buildTree(List<Book> bookList) {
        for (Book book : bookList) {
            if (book.parentId == null) {
                return new TreeNode(book, buildChildList(bookList, book.id));
            }
        }
        return null;
    }

    public static List<TreeNode> buildChildList(List<Book> bookList, Integer parentId) {
        List<TreeNode> list = new ArrayList<>();
        for (Book book : bookList) {
            if (parentId != null && parentId.equals(book.parentId)) {
                list.add(new TreeNode(book, buildChildList(bookList, book.id)));
            }
        }
        return list;
    }

    public static int getPrice(String code) {
        List<Book> list = new ArrayList<Book>(){{
            add(new Book(1, "高等数学", 5, 10, true));
            add(new Book(2, "线性代数", 5, 20, true));
            add(new Book(3, "唐诗", 6, 15, true));
            add(new Book(4, "宋词", 6, 14, true));
            add(new Book(5, "数学类", 7, -3, true));
            add(new Book(6, "语文类", 7, -2, true));
            add(new Book(7, "教材类", null, -5, true));
        }};
        TreeNode root = buildTree(list);
        TreeNode treeNode = findTreeNode(root, code);
        if (treeNode != null) {
            return getTreeNodePrice(treeNode);
        }
        return 0;
    }

    public static TreeNode findTreeNode(TreeNode root, String code) {
        if (root != null) {
            if (code.equals(root.value.code)){
                return root;
            }
            if (root.childrenList != null && root.childrenList.size() > 0) {
                for (TreeNode treeNode : root.childrenList) {
                    TreeNode res;
                    if ((res = findTreeNode(treeNode, code)) != null) {
                        return res;
                    }
                }
            }
        }
        return null;
    }


    public static int getTreeNodePrice(TreeNode treeNode) {
        int res = treeNode.value.price;
        if (treeNode.childrenList != null && treeNode.childrenList.size() > 0) {
            for (TreeNode child : treeNode.childrenList) {
                res += getTreeNodePrice(child);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        System.out.println(getPrice("教材类"));
    }


}
