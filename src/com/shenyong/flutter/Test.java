package com.shenyong.flutter;

public class Test {
    private static final String PATTERN = "^asset(s)?(/([-\\w]+|\\d\\.\\dx))*/[-\\w]+\\.(jp(e)?g|png|webp)$";
    public static void main(String[] args) {
        System.out.println("assets/my_icon.png".matches(PATTERN));
        System.out.println("assets/my_icon.png".replaceAll("[-\\w]+\\.(jp(e)?g|png|webp)", "icon1.png"));
        System.out.println("assets/images/doge.jpeg".matches(PATTERN));
        System.out.println("assets/images/doge.jpg".matches(PATTERN));
        System.out.println("assets/images-1/3.0x/doge.jpeg".matches(PATTERN));
        System.out.println("assets/images/3.0x/doge-1.jpeg".matches(PATTERN));
    }
}
