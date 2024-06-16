import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
//TIP 要<b>运行</b>代码，请按 <shortcut actionId="Run"/> 或
// 点击装订区域中的 <icon src="AllIcons.Actions.Execute"/> 图标。
/*
 *程序首先让用户选择或输入文本文件的位置和文件名。也可以参数的
 *形式，在启动程序时提供文件路径和文件名。
 *▪ 程序读入文本数据，进行分析，将其转化为有向图：
 *– 有向图的节点为文本中包含的某个单词（不区分大小写）
 *– 两个节点A,B之间存在一条边A→B，意味着在文本中至少有一处位置A和B
 *- 相邻出现（即A和B之间有且仅有1或多个空格）。
 *– A→B的权重w=文本中A和B相邻出现的次数，w>=1
 * */


final class Main {
    private static Map<String, Map<String, Integer>> graph;
    private static final SecureRandom secureRandom = new SecureRandom();
    // 获取 graph 的方法
    Main() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static String getString(String filePath) {
        StringBuilder text = new StringBuilder();
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line);
                text.append(" ");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    public static String cleanText(String text) {
        //去除文本中除了字母和空格之外的字符
        StringBuilder sb = new StringBuilder();
        char prevChar = ' ';
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isLetter(c) || c == ' ') {
                if (c == ' ' && prevChar == ' ') {
                    continue;
                } else {
                    c = Character.toLowerCase(c);
                    sb.append(c);
                }
                prevChar = c;
            } else {
                if (prevChar != ' ') {
                    prevChar = ' ';
                    sb.append(' ');
                }
            }
        }
        return sb.toString();
    }

    public static Map<String, Map<String, Integer>> analyzeText(String text) {
        // 将文本转化为有向图：节点为单词，A到B有一条边当前仅当A为B的前一个单词
        // 权重为A在B之前出现的次数
        // 相邻出现（即A和B之间有且仅有1或多个空格）。
        // 生成有向图
        // 时间复杂度：O(n^2)
        Map<String, Map<String, Integer>> hashMap = new HashMap<>();
        String[] words = text.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i != words.length - 1) {
                String nextWord = words[i + 1];
                //判断word是否存在于图中
                if (!hashMap.containsKey(word)) {
                    //如果不存在，则添加到图中
                    Map<String, Integer> map = new HashMap<>();
                    map.put(nextWord, 1);
                    hashMap.put(word, map);
                } else {
                    //先判断对应的nextWord是否存在于图中
                    boolean isNextWordInGraph = false;
                    for (Map.Entry<String, Integer> entry : hashMap.get(word).entrySet()) {
                        if (entry.getKey().equals(nextWord)) {
                            //如果存在，则将权重+1
                            int weight = entry.getValue() + 1;
                            hashMap.get(word).put(nextWord, weight);
                            isNextWordInGraph = true;
                            break;
                        }   //如果不存在，则添加到图中
                    }
                    if (!isNextWordInGraph) {
                        hashMap.get(word).put(nextWord, 1);
                    }
                }
            } else {
                //如果word是最后一个单词，则不再有后继节点
                if (!hashMap.containsKey(word)) {
                    Map<String, Integer> map = new HashMap<>();
                    hashMap.put(word, map);
                }
            }
        }
        return hashMap;
    }

    public static void showDirectedGraph() {
        for (Map.Entry<String, Map<String, Integer>> entry : getGraph().entrySet()) {
            String key = entry.getKey();
            Map<String, Integer> value = entry.getValue();
            System.out.print(key + " → ");
            for (Map.Entry<String, Integer> entry2 : value.entrySet()) {
                String key2 = entry2.getKey();
                int value2 = entry2.getValue();
                System.out.print(key2 + " (" + value2 + ") ");
            }
            System.out.println();
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        // 查询两个单词之间是否存在桥词
        // word1、word2的桥接词word3：图中存在两条边word1→word3，word3→word2,
        // 则word3为word1和word2的桥词
        // 算法：在图中查询word1的后继节点，查询word2的前驱节点，如果存在word3，则word3为word1和word2的桥词
        // 时间复杂度：O(n)
        String string;
        if (isWordInGraph(word1) && isWordInGraph(word2)) {
            //查询word1的后继节点
            String[] nextWord1 = queryNextWord(word1);
            if (nextWord1 == null || nextWord1.length == 0) {
                string = "No bridge words from " + word1 + " to " + word2 + "!";
                return string;
            }
            //查询word2的前驱节点
            String[] prevWord2 = queryPrevWord(word2);
            String[] bridgeWords = new String[nextWord1.length];
            if (prevWord2 == null || prevWord2.length == 0) {
                string = "No bridge words from " + word1 + " to " + word2 + "!";
                return string;
            }
            //查询word1和word2的桥词,即判断nextWord1和prevWord2是否存在word3
            int i = 0;
            for (String word3 : nextWord1) {
                if (Arrays.asList(prevWord2).contains(word3)) {
                    bridgeWords[i] = word3;
                    i++;
                }
            }
            if (bridgeWords[0] == null) {
                string = "No bridge words from " + word1 + " to " + word2 + "!";
            }
            else {
                if (bridgeWords[1] == null) {
                    string = "The bridge words from " + word1 + " to " + word2 + " is: " + bridgeWords[0];
                }
                else {
                    string = "The bridge words from " + word1 + " to " + word2 + " are: ";
                    StringBuilder sb = new StringBuilder();
                    for (String bridgeWord : bridgeWords) {
                        if (bridgeWord == null) {
                            break;
                        }
                        sb.append(bridgeWord).append(" ");
                    }
                    string = string + sb.toString();
                }
            }
            return string;
        }
        if (!isWordInGraph(word1) && !isWordInGraph(word2)) {
            string = "No " + word1 + " and " + word2 + " in the graph!";
        }
        else {
            if (!isWordInGraph(word1)) {
                string = "No " + word1 + " in the graph!";
            }
            else {
                string = "No " + word2 + " in the graph!";
            }
        }
        return string;
    }
    public static String[] queryNextWord(String word) {
        /*
         查询word的后继词
         算法：在图中查询word的后继节点，返回所有的一个节点
        */
        //查询word的后继节点
        // 时间复杂度：O(n)
        if (getGraph().containsKey(word)) {
            List<String> nextWords = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : getGraph().get(word).entrySet()) {
                nextWords.add(entry.getKey());
            }
            return nextWords.toArray(new String[0]);
        }
        return null;
    }

    public static String[] queryPrevWord(String word) {
        /*
         查询word的前驱词
         算法：在图中查询word的前驱节点，返回所有的一个节点
        */
        //时间复杂度：O(n)
        List<String> prevWords = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : getGraph().entrySet()) {
            for (Map.Entry<String, Integer> entry2 : entry.getValue().entrySet()) {
                if (entry2.getKey().equals(word)) {
                    prevWords.add(entry.getKey());
                }
            }
        }
        if (prevWords.isEmpty()) {
            return null;
        }
        return prevWords.toArray(new String[0]);
    }

    //查询word是否存在于图中
    public static boolean isWordInGraph(String word) {
        for (Map.Entry<String, Map<String, Integer>> entry : getGraph().entrySet()) {
            if (entry.getKey().equals(word) || entry.getValue().containsKey(word)) {
                return true;
            }
        }
        return false;
    }

    public static String generateNewText(String inputText) {
        //根据桥接词生成新文本
        //算法：对inputText中相邻的两个单词查询其桥词，如果存在，则将这个桥词插入到两个单词之间
        String[] words = inputText.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];
            String result = queryBridgeWords(word1, word2);
            if (result.contains("bridge words")) {
                String[] bridgeWords = result.split(":")[1].trim().split(" ");
                sb.append(word1).append(" ").append(bridgeWords[0]).append(" ");
            } else {
                sb.append(word1).append(" ");
            }
        }
        sb.append(words[words.length - 1]);
        return sb.toString();
    }


    public static String randomWalk(String randomWord, Map<String, Map<String, Integer>> tempGraph) {
        //随机游走算法
        //算法：从图中随机选择一个节点，随机选择一个边，然后随机选择一个节点，直到遇到已游走的边
        // 时间复杂度：O(n)
        //从graph中随机选择一个节点
        //随机选择一个边
        String[] nextWords = queryNextWord(randomWord);
        //.如果没有后继节点，则结束
        if (nextWords == null || nextWords.length == 0) {
            return null;
        }
        int randomIndex = generateRandomIndex(nextWords.length);
        String nextWord = nextWords[randomIndex];
        //判断边是否已经访问过,即其权重是否为0
        if (isAlreadyVisited(randomWord, nextWord, tempGraph)) {
            return nextWord + "0";
        }
        //将改边的权重设为0
        tempGraph.get(randomWord).put(nextWord, 0);
        //用户输入是否继续随机游走
        return nextWord;
    }

    public static String getRandomWord() {
        //随机选择一个单词
        //算法：从graph中随机选择一个节点
        String randomWord = "";
        List<Map.Entry<String, Map<String, Integer>>> edges = new ArrayList<>(getGraph().entrySet());
        Collections.shuffle(edges); //随机打乱顺序
        for (Map.Entry<String, Map<String, Integer>> e : edges) {
            randomWord = e.getKey();
            break;
        }
        return randomWord;
    }

    public static boolean isAlreadyVisited(String currentWord, String nextWord, Map<String, Map<String, Integer>> tempGraph) {
        //判断边是否已经访问过,即其权重是否为0
        return tempGraph.get(currentWord).get(nextWord) == 0;
    }

    public static int generateRandomIndex(int arrayLength) {
        return secureRandom.nextInt(arrayLength);
    }

    public static Map<String, Map<String, Integer>> getGraph() {
        return graph;
    }

    public static void setGraph(Map<String, Map<String, Integer>> newGraph) {
        Main.graph = newGraph;
    }

}

final class GraphVisualization {
    private static int count = 0;
    private GraphVisualization() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static String graphVisualization(Map<String, Map<String, Integer>> inputGraph, String startNode) {
        // 创建示例图
        Map<String, Map<String, Integer>> tempGraph = inputGraph;
        // 进行深度优先搜索并输出图形化结果
        System.out.println("Graph Visualization:");
        return writeDotFile(createDotFile(tempGraph, startNode));
    }

    public static String createDotFile(Map<String, Map<String, Integer>> inputGraph, String startNode) {
        char newLine = '\n';
        StringBuilder dotText = new StringBuilder();    //StringBuilder在这里效率要高于用String加加加
        dotText.append(String.format("digraph G{" + newLine));    //写入开头
        for (String node : inputGraph.keySet()) {
            dotText.append("\t").append(node);
            if (Objects.equals(node, startNode)) {
                dotText.append(" [style=filled, fillcolor=red]");
            }
            dotText.append(";").append(newLine);
        }
        dotText.append(newLine);
        for (String node : inputGraph.keySet()) {
            Map<String, Integer> neighbors = inputGraph.get(node);
            if (neighbors != null) {
                for (String neighbor : neighbors.keySet()) {
                    int weight = neighbors.get(neighbor);
                    dotText.append("\t");
                    dotText.append(String.format("%s->%s[label=%d]", node, neighbor, weight));
                    dotText.append(";").append(newLine);
                }
            }
        }
        dotText.append("}").append(newLine);    //写入结束
        return dotText.toString();
    }

    public static String writeDotFile(String dotText) {
        String tmpDir = System.getProperty("user.dir");
        String graphFilePath = tmpDir + "graph.gv";
        try {
            File tmpfile = new File(tmpDir);
            if (!tmpfile.exists()) {
                boolean success = tmpfile.mkdirs();
                if (!success) {
                    throw new IOException("创建临时目录失败");
                }
            }
            FileWriter fw = new FileWriter(graphFilePath, StandardCharsets.UTF_8);
            System.out.println("Writing graph to " + graphFilePath);
            BufferedWriter bufWriter = new BufferedWriter(fw);
            bufWriter.write(dotText);
            bufWriter.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to open file");
        }
        return runGraphViz(graphFilePath, tmpDir);
    }
    public static String changePathColor(List<List<String>> paths) {
        String tmpDir = System.getProperty("user.dir");
        String graphFilePath = tmpDir + "graph.gv";
        //读取文件内容

        try {
            File tmpfile = new File(tmpDir);
            if (!tmpfile.exists()) {
                boolean success = tmpfile.mkdirs();
                if (!success) {
                    throw new IOException("创建临时目录失败");
                }
            }
            File graphFile = new File(graphFilePath);
            if (!graphFile.exists()) {
                boolean success = graphFile.mkdirs();
                if (!success) {
                    throw new IOException("创建临时目录失败");
                }
            }

            FileReader fr = new FileReader(graphFilePath,StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            List<String> lines = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
            fr.close();
            //修改路径颜色
            for (List<String> path : paths) {
                String color = "blue";
                for (int i = 0; i < lines.size(); i++) {
                    String[] words = lines.get(i).split("->");
                    if (words.length == 2) {
                        String node1 = words[0].trim();
                        String node2 = words[1].trim().split("\\[")[0];
                        if (path.contains(node1) && path.contains(node2)) {
                            //words[1] = "worlds[label=1];"
                            words[1] = words[1].split("\\]")[0];
                            words[1] = words[1] + ", color=" + color + "]" + ";";
                            lines.set(i, words[0] + "->" + words[1]);
                        }
                    }
                }
            }
            //写入文件
            FileWriter fw = new FileWriter(tmpDir + "graphChangeColor.gv",StandardCharsets.UTF_8);
            BufferedWriter bufWriter = new BufferedWriter(fw);
            for (String line2 : lines) {
                bufWriter.write(line2);
                bufWriter.newLine();
            }
            bufWriter.close();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to open file");
        }
        return runGraphViz(tmpDir + "graphChangeColor.gv", tmpDir + "ChangeColor" + count++);
    }

    public static String runGraphViz(String filename, String tmpDir) {
        Runtime rt = Runtime.getRuntime(); // 使用Runtime执行cmd命令
        try {
            String dotForWindows = "D:\\else\\Graphviz\\bin\\dot.exe";
            String[] args = {dotForWindows, filename, "-Tpng", "-o", tmpDir + "img.png"};
            Process process = rt.exec(args);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Graphviz进程退出，退出代码：" + exitCode);
            }
            return tmpDir + "img.png";
        } catch (IOException e) {
            throw new RuntimeException("执行Graphviz命令失败。", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            throw new RuntimeException("Graphviz命令执行被中断。", e);
        }
    }

}

final class Lab1Window {
    static final String[] IMAGE_PATH = new String[1];
    private Lab1Window() {
        throw new UnsupportedOperationException("Utility class");
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(1000, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true); // make the frame visible
        frame.setLocationRelativeTo(null); // 中心显示
        frame.setTitle("Lab1"); //设置标题
        //添加六个按钮
        JButton button1 = new JButton("选择文件");
        button1.setHorizontalAlignment(SwingConstants.LEFT);
        button1.setBounds(0, 10, 100, 50);
        frame.add(button1);
        JButton button2 = new JButton("展示有向图");
        button2.setHorizontalAlignment(SwingConstants.LEFT);
        button2.setBounds(100, 10, 100, 50);
        frame.add(button2);
        JButton button3 = new JButton("查询桥接词");
        button3.setBounds(200, 10, 100, 50);
        frame.add(button3);
        JButton button4 = new JButton("产生新文本");
        button4.setBounds(300, 10, 100, 50);
        frame.add(button4);
        JButton button5 = new JButton("计算最短路径");
        button5.setBounds(400, 10, 100, 50);
        frame.add(button5);
        JButton button6 = new JButton("随机游走");
        button6.setBounds(500, 10, 100, 50);
        frame.add(button6);
        //添加一个文本框
        JTextArea statusText = new JTextArea();
        statusText.setBounds(600, 10, 400, 50);
        statusText.setEditable(false);
        statusText.setBorder(BorderFactory.createLineBorder(Color.black));
        //设置字体自动换行
        statusText.setLineWrap(true);
        frame.add(statusText);
        //添加一个标签，用于显示图片
        ImageIcon icon = new ImageIcon(IMAGE_PATH[0]);
        JLabel label = new JLabel(icon);
        label.setBounds(0, 60, 800, 500);
        frame.add(label);
        //绑定动作
        final String[] cleanedText = {""};
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("选择文件");
                String filePath = selectFile();
                statusText.setText("选择文件成功");
                String text = Main.getString(filePath);
                cleanedText[0] = Main.cleanText(text);
                System.out.println(cleanedText[0]);
                String startWord = cleanedText[0].split(" ")[0];
                statusText.setText("正在分析文本....");
                Main.setGraph(Main.analyzeText(cleanedText[0]));
                IMAGE_PATH[0] = GraphVisualization.graphVisualization(Main.getGraph(), startWord);
                statusText.setText("分析文本完成！");
                Main.showDirectedGraph();
            }
        });
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("生成有向图");
                if (IMAGE_PATH[0] != null) {
                    ImageIcon icon = new ImageIcon(IMAGE_PATH[0]);
                    //跳转icon缩放
                    Image image = icon.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
                    icon = new ImageIcon(image);
                    label.setIcon(icon);
                    label.setVisible(true);
                    statusText.setText("展示有向图成功");
                }
            }
        });
        button3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("查询桥接词");
                String word1 = JOptionPane.showInputDialog("请输入第一个词");
                String word2 = JOptionPane.showInputDialog("请输入第二个词");
                if (Main.getGraph() == null) {
                    statusText.setText("请先选择文件并生成有向图");
                } else {
                    String result = Main.queryBridgeWords(word1, word2);
                    statusText.setText(result);
                }
            }
        });
        button4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("产生新文本");

                String inputText = JOptionPane.showInputDialog("请输入一段句子");
                if (Main.getGraph() == null) {
                    statusText.setText("请先选择文件并生成有向图");
                } else {
                    String newText = Main.generateNewText(inputText);
                    statusText.setText(newText);
                }
            }
        });
        button5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("计算最短路径");
                if (Main.getGraph() == null) {
                    statusText.setText("请先选择文件并生成有向图");
                } else {
                    statusText.setText(theRoad(Main.getGraph()));
                }
                //刷新图片
                ImageIcon icon = new ImageIcon(IMAGE_PATH[0]);
                label.setIcon(icon);
                IMAGE_PATH[0] = GraphVisualization.graphVisualization(Main.getGraph(), "start");
                label.setVisible(true);
            }
        });
        button6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                System.out.println("随机游走");
                if (Main.getGraph() == null) {
                    statusText.setText("请先选择文件并生成有向图");
                } else {
                    String randomWord = Main.getRandomWord();
                    statusText.setText("随机游走开始！\n");
                    statusText.append(randomWord);
                    Map<String, Map<String, Integer>> tempGraph = Main.getGraph();
                    while (true) {
                        //用户选择是否继续游走
                        int choice = JOptionPane.showConfirmDialog(null, "是否继续随机游走？", "随机游走", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.NO_OPTION) {
                            statusText.append("\n随机游走结束！");
                            break;
                        }
                        String nextWord = Main.randomWalk(randomWord, tempGraph);
                        if (nextWord == null) {
                            statusText.append("\n随机游走结束！");
                            break;
                        }
                        if (nextWord.endsWith("0")) {
                            statusText.append(" " + nextWord.substring(0, nextWord.length() - 1));
                            statusText.append("\n");
                            statusText.append("\n随机游走结束");
                            break;
                        }
                        statusText.append(" " + nextWord);
                        randomWord = nextWord;
                    }
                    Main.setGraph(Main.analyzeText(cleanedText[0]));
                }
            }
        });
    }
    public static String selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            System.out.println(fileChooser.getSelectedFile().getAbsolutePath());
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    // 计算从源节点到目标节点的所有最短路径
    public static List<List<String>> calcShortestPath(Map<String, Map<String, Integer>> graph,
                                                      String source, String target) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, List<String>> predecessors = new HashMap<>();
        List<List<String>> calcShortestPath = new ArrayList<>();
        Queue<String> unvisited = new PriorityQueue<>(Comparator.comparingInt(distance::get));

        for (String node : graph.keySet()) {
            distance.put(node, node.equals(source) ? 0 : Integer.MAX_VALUE);
            unvisited.add(node);
        }

        while (!unvisited.isEmpty()) {
            String current = unvisited.poll();
            if (current.equals(target)) {
                break;
            }
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors != null) {
                for (String neighbor : neighbors.keySet()) {
                    int newDistance = distance.get(current) + neighbors.get(neighbor);
                    if (newDistance < distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        distance.put(neighbor, newDistance);
                        predecessors.put(neighbor, new ArrayList<>(Collections.singletonList(current)));
                        // 更新优先级队列
                        unvisited.remove(neighbor);
                        unvisited.add(neighbor);
                    } else if (newDistance == distance.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                        predecessors.get(neighbor).add(current);
                    }
                }
            }
        }
        buildPaths(source, target, predecessors, new ArrayList<>(), calcShortestPath, new HashSet<>());
        return calcShortestPath;
    }

    private static void buildPaths(String source, String current, Map<String, List<String>> predecessors,
                                   List<String> path, List<List<String>> calcShortestPath, Set<String> visited) {
        path.add(current);
        visited.add(current);
        if (current.equals(source)) {
            List<String> shortestPath = new ArrayList<>(path);
            Collections.reverse(shortestPath);
            calcShortestPath.add(shortestPath);
        } else {
            List<String> predecessorsList = predecessors.get(current);
            if (predecessorsList != null) {
                for (String predecessor : predecessorsList) {
                    if (!visited.contains(predecessor)) {
                        buildPaths(source, predecessor, predecessors, path, calcShortestPath, visited);
                    }
                }
            }
        }
        path.remove(path.size() - 1);
        visited.remove(current);  // 递归返回时将节点标记为未访问
    }

    public static String theRoad(Map<String, Map<String, Integer>> graph) {
        // 输入节点
        String source = JOptionPane.showInputDialog("Enter source node: ");
        String target = JOptionPane.showInputDialog("Enter target node: ");
        StringBuilder sb = new StringBuilder();
        if (source.equals(target)) {
            // 如果两次输入的节点相同，计算该节点到图中其他任一节点的最短路径
            System.out.println("Shortest paths from " + source + " to any other node:");
            sb.append("Shortest paths from ").append(source).append(" to any other node:\n");
            for (String node : graph.keySet()) {
                if (!node.equals(source)) {
                    List<List<String>> paths = calcShortestPath(graph, source, node);
                    for (List<String> path : paths) {
                        System.out.println(path);
                        sb.append(path);
                    }
                }
            }
        } else {
            // 计算源节点到目标节点的最短路径
            List<List<String>> paths = calcShortestPath(graph, source, target);
            if (paths.isEmpty()) {
                System.out.println("No path found.");
                sb.append("No path found.");
            } else {
                System.out.println("Shortest paths from " + source + " to " + target + ":");
                sb.append("Shortest paths from ").append(source).append(" to ").append(target).append(":\n");
                for (List<String> path : paths) {
                    System.out.println(path);
                    sb.append(path);
                }
                IMAGE_PATH[0] = GraphVisualization.changePathColor(paths);
            }
        }
        return sb.toString();
    }
}

