import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainTest {

    @Test
    public void calcShortestPath1() {
        String word1 = "new";
        String word2 = "to";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("The bridge words from new to to are: worlds civilizations ",words);
    }

    @Test
    public void calcShortestPath2() {
        String word1 = "life";
        String word2 = "and";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("No bridge words from life to and!",words);
    }

    @Test
    public void calcShortestPath3() {
        String word1 = "new";
        String word2 = "we";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("No we in the graph!",words);
    }

    @Test
    public void calcShortestPath4() {
        String word1 = "life";
        String word2 = "123";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("No 123 in the graph!",words);
    }
    @Test
    public void calcShortestPath5() {
        String word1 = "we";
        String word2 = "are";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("No we and are in the graph!",words);
    }
    @Test
    public void calcShortestPath6() {
        String word1 = "we";
        String word2 = "456";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("No we and 456 in the graph!",words);
    }
    @Test
    public void calcShortestPath7() {
        String word1 = "we12";
        String word2 = "are12";
        String words = Main.queryBridgeWords(word1,word2);
        assertEquals("No we12 and are12 in the graph!",words);
    }
    @BeforeClass
    public static void set_graph(){
        String path = "D:/text.txt";
        Main.setGraph(Main.analyzeText(Main.cleanText(Main.getString(path))));
    }

}