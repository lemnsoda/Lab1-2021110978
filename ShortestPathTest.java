import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ShortestPathTest {

    @Test
    public void testPath1() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph.put("A", Map.of("B", 1));
        graph.put("B", new HashMap<>());
        graph.put("C", new HashMap<>());

        List<List<String>> expected = List.of(List.of("A", "B"));
        List<List<String>> actual = Lab1Window.calcShortestPath(graph, "A", "B");
        assertEquals(expected, actual);
    }

    @Test
    public void testPath2() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph.put("A", Map.of("B", 1, "C", 4));
        graph.put("B", Map.of("C", 2));
        graph.put("C", new HashMap<>());

        List<List<String>> expected = List.of(List.of("A", "B", "C"));
        List<List<String>> actual = Lab1Window.calcShortestPath(graph, "A", "C");
        assertEquals(expected, actual);
    }

    @Test
    public void testPath3() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph.put("A", Map.of("B", 2, "D", 4));
        graph.put("B", Map.of("C", 1));
        graph.put("C", Map.of("D", 1));
        graph.put("D", new HashMap<>());

        List<List<String>> expected = List.of(List.of("A", "D"), List.of("A", "B", "C", "D"));
        List<List<String>> actual = Lab1Window.calcShortestPath(graph, "A", "D");
        assertEquals(expected, actual);
    }

    @Test
    public void testPath4() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph.put("A", Map.of("B", 1));
        graph.put("B", new HashMap<>());
        graph.put("C", new HashMap<>());
        graph.put("D", new HashMap<>());

        List<List<String>> expected = List.of();
        List<List<String>> actual = Lab1Window.calcShortestPath(graph, "A", "D");
        assertEquals(expected, actual);
    }

    @Test
    public void testComprehensivePath() {
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph.put("A", Map.of("B", 1, "C", 3));
        graph.put("B", Map.of("C", 2, "D", 5));
        graph.put("C", Map.of("D", 1));
        graph.put("D", new HashMap<>());

        List<List<String>> expected = List.of(List.of("A", "C", "D"), List.of("A", "B", "C", "D"));
        List<List<String>> actual = Lab1Window.calcShortestPath(graph, "A", "D");
        assertEquals(expected, actual);
    }
}
