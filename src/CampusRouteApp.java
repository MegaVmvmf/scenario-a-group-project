import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CampusRouteApp {

    public static class StringList {
        private final ArrayList inner = new ArrayList();

        public void add(String s) {
            inner.add(s);
        }

        public String get(int i) {
            return (String) inner.get(i);
        }

        public int size() {
            return inner.size();
        }

        public String join(String sep) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < inner.size(); i++) {
                if (i > 0) sb.append(sep);
                sb.append(get(i));
            }
            return sb.toString();
        }
    }

    public static class BoolList {
        private final ArrayList inner = new ArrayList();

        public void add(boolean b) {
            inner.add(Boolean.valueOf(b));
        }

        public boolean get(int i) {
            return ((Boolean) inner.get(i)).booleanValue();
        }

        public int size() {
            return inner.size();
        }
    }

    public static class OptionList {
        private final ArrayList inner = new ArrayList();

        public void add(CommuteOption o) {
            inner.add(o);
        }

        public CommuteOption get(int i) {
            return (CommuteOption) inner.get(i);
        }

        public int size() {
            return inner.size();
        }

        public boolean isEmpty() {
            return inner.isEmpty();
        }

        public void sortByCost() {
            for (int i = 0; i < inner.size(); i++) {
                for (int j = i + 1; j < inner.size(); j++) {
                    if (get(j).fare < get(i).fare) {
                        Object tmp = inner.get(i);
                        inner.set(i, inner.get(j));
                        inner.set(j, tmp);
                    }
                }
            }
        }

        public void sortByTime() {
            for (int i = 0; i < inner.size(); i++) {
                for (int j = i + 1; j < inner.size(); j++) {
                    if (get(j).totalTime < get(i).totalTime) {
                        Object tmp = inner.get(i);
                        inner.set(i, inner.get(j));
                        inner.set(j, tmp);
                    }
                }
            }
        }
    }

    // 1. Model for Commute Choices
    public static class CommuteOption {
        public final String name;
        public final boolean operating;
        public final boolean fareOk;
        public final boolean deadlineOk;
        public final boolean walkOk;
        public final boolean eligible;
        public final double fare;
        public final int rideTime;
        public final int walkTime;
        public final int totalTime;
        public final LocalTime arrival;

        public CommuteOption(String name, boolean operating, double fare, int rideTime, int walkTime,
                             int totalTime, LocalTime arrival, boolean fareOk, boolean deadlineOk, boolean walkOk) {
            this.name = name;
            this.operating = operating;
            this.fare = fare;
            this.rideTime = rideTime;
            this.walkTime = walkTime;
            this.totalTime = totalTime;
            this.arrival = arrival;
            this.fareOk = fareOk;
            this.deadlineOk = deadlineOk;
            this.walkOk = walkOk;
            this.eligible = operating && fareOk && deadlineOk && walkOk;
        }

        public String failureReason() {
            StringList reasons = new StringList();
            if (!operating) reasons.add("Suspended");
            if (!fareOk) reasons.add("Over Budget");
            if (!deadlineOk) reasons.add("Misses Deadline");
            if (!walkOk) reasons.add("Walk exceeds weather limit");
            return reasons.join(", ");
        }
    }

    // 2. Tree Node
    public static class ExprNode {
        public final String val;
        public final ExprNode left;
        public final ExprNode right;

        public ExprNode(String val) {
            this(val, null, null);
        }

        public ExprNode(String val, ExprNode left, ExprNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    // 3. Graphics - Bro :(
    public static class VisualCanvas extends JPanel {
        private ExprNode astRoot;
        private CommuteOption trainOpt;
        private CommuteOption busOpt;
        private CommuteOption rideOpt;
        private CommuteOption bestOpt;
        private String currentWeather = "";
        private LocalTime deadlineTime = null;

        public void updateData(ExprNode astRoot, CommuteOption trainOpt, CommuteOption busOpt,
                               CommuteOption rideOpt, CommuteOption bestOpt,
                               String currentWeather, LocalTime deadlineTime) {
            this.astRoot = astRoot;
            this.trainOpt = trainOpt;
            this.busOpt = busOpt;
            this.rideOpt = rideOpt;
            this.bestOpt = bestOpt;
            this.currentWeather = currentWeather;
            this.deadlineTime = deadlineTime;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int colW = w / 3;

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(colW, 10, colW, h - 10);
            g2.drawLine(colW * 2, 10, colW * 2, h - 10);

            // Column 1: Expression Tree (Step 6)
            g2.setColor(new Color(30, 58, 138));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString("Step 6: Expression Tree (AST)", 15, 25);
            if (astRoot != null) {
                drawASTNode(g2, astRoot, colW / 2 + 30, 65, Math.max(30, colW / 5));
            } else {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                g2.drawString("Select Weather & Priority to render AST.", 15, 65);
            }

            // Column 2: Sequential Decision Tree
            g2.setColor(new Color(30, 58, 138));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString("Step 7: Sequential Decision Tree", colW + 15, 25);
            drawDecisionTree(g2, colW + 15, 55, colW - 30);

            // Column 3: Knowledge Graph
            g2.setColor(new Color(30, 58, 138));
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            g2.drawString("Step 8: Commute Knowledge Graph", colW * 2 + 15, 25);
            drawKnowledgeGraph(g2, colW * 2 + 15, 55, colW - 30);
        }

        private void drawASTNode(Graphics2D g2, ExprNode node, int x, int y, int dx) {
            if (node == null) return;
            g2.setColor(Color.DARK_GRAY);
            if (node.left != null) {
                g2.drawLine(x, y, x - dx, y + 60);
                drawASTNode(g2, node.left, x - dx, y + 60, Math.max(25, dx / 2));
            }
            if (node.right != null) {
                g2.drawLine(x, y, x + dx, y + 60);
                drawASTNode(g2, node.right, x + dx, y + 60, Math.max(25, dx / 2));
            }

            boolean isOp = "AND".equals(node.val);
            g2.setColor(isOp ? new Color(37, 99, 235) : new Color(226, 232, 240));
            g2.fillOval(x - 24, y - 16, 48, 32);
            g2.setColor(Color.BLACK);
            g2.drawOval(x - 24, y - 16, 48, 32);

            g2.setColor(isOp ? Color.WHITE : Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(node.val, x - fm.stringWidth(node.val) / 2, y + 4);
        }

        private void drawDecisionTree(Graphics2D g2, int x0, int y0, int width) {
            CommuteOption target = (bestOpt != null) ? bestOpt : trainOpt;
            if (target == null) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                g2.drawString("Awaiting commute input values...", x0, y0 + 15);
                return;
            }

            int cx = x0 + width / 2;
            Color green = new Color(22, 163, 74);
            Color red = new Color(220, 38, 38);

            drawStepBox(g2, cx - 65, y0, 130, 32, "1. Service Operating?", Color.WHITE);
            g2.setColor(target.operating ? green : red);
            g2.drawLine(cx, y0 + 32, cx, y0 + 68);
            g2.drawString(target.operating ? "YES" : "NO (Stop)", cx + 6, y0 + 54);

            drawStepBox(g2, cx - 65, y0 + 68, 130, 32, "2. Fare <= Budget?",
                    target.operating ? Color.WHITE : new Color(241, 245, 249));
            g2.setColor(target.operating && target.fareOk ? green : red);
            g2.drawLine(cx, y0 + 100, cx, y0 + 136);
            g2.drawString(target.fareOk ? "YES" : "NO (Over)", cx + 6, y0 + 122);

            drawStepBox(g2, cx - 65, y0 + 136, 130, 32, "3. Meets Deadline?",
                    target.operating && target.fareOk ? Color.WHITE : new Color(241, 245, 249));
            g2.setColor(target.operating && target.fareOk && target.deadlineOk ? green : red);
            g2.drawLine(cx, y0 + 168, cx, y0 + 204);
            g2.drawString(target.deadlineOk ? "YES" : "NO (Late)", cx + 6, y0 + 190);

            drawStepBox(g2, cx - 65, y0 + 204, 130, 32, "4. Walk OK -> GO",
                    target.eligible ? new Color(220, 252, 231) : new Color(254, 226, 226));

            g2.setColor(Color.DARK_GRAY);

            g2.setFont(new Font("SansSerif", Font.ITALIC, 11));
            g2.drawString("Tracing candidate: " + target.name, x0 + 10, y0 + 265);
        }

        private void drawStepBox(Graphics2D g2, int x, int y, int w, int h, String text, Color bg) {
            g2.setColor(bg);
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(x, y, w, h, 8, 8);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + 20);
        }

        private void drawKnowledgeGraph(Graphics2D g2, int x0, int y0, int width) {
            if (trainOpt == null || busOpt == null || rideOpt == null || deadlineTime == null) {
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
                g2.drawString("Awaiting input values...", x0, y0 + 15);
                return;
            }
            DateTimeFormatter fmt12 = DateTimeFormatter.ofPattern("h:mm a");
            int userX = x0 + 35;
            int userY = y0 + 130;
            int midX = x0 + width / 2;
            int trainY = y0 + 45;
            int busY = y0 + 130;
            int rideY = y0 + 215;
            int campusX = x0 + width - 40;
            int campusY = y0 + 130;
            int weatherX = x0 + width - 55;
            int weatherY = y0 + 35;
            int deadlineX = x0 + width - 55;
            int deadlineY = y0 + 225;
            drawEdge(g2, userX, userY, midX, trainY, String.format("$%.2f", trainOpt.fare));
            drawEdge(g2, userX, userY, midX, busY, String.format("$%.2f", busOpt.fare));
            drawEdge(g2, userX, userY, midX, rideY, String.format("$%.2f", rideOpt.fare));
            drawEdge(g2, midX, trainY, campusX, campusY, trainOpt.totalTime + "m");
            drawEdge(g2, midX, busY, campusX, campusY, busOpt.totalTime + "m");
            drawEdge(g2, midX, rideY, campusX, campusY, rideOpt.totalTime + "m");
            drawEdge(g2, weatherX, weatherY, midX, trainY, currentWeather);
            drawEdge(g2, deadlineX, deadlineY, campusX, campusY, deadlineTime.format(fmt12));
            drawGraphNode(g2, userX, userY, "User", new Color(37, 99, 235));
            drawGraphNode(g2, midX, trainY, "Train", trainOpt.eligible ? new Color(22, 163, 74) : Color.GRAY);
            drawGraphNode(g2, midX, busY, "Bus", busOpt.eligible ? new Color(22, 163, 74) : Color.GRAY);
            drawGraphNode(g2, midX, rideY, "Ride", rideOpt.eligible ? new Color(22, 163, 74) : Color.GRAY);
            drawGraphNode(g2, campusX, campusY, "Campus", new Color(15, 23, 42));
            drawGraphNode(g2, weatherX, weatherY, "Weather", new Color(217, 119, 6));
            drawGraphNode(g2, deadlineX, deadlineY, "Deadline", new Color(220, 38, 38));
        }

        private void drawEdge(Graphics2D g2, int x1, int y1, int x2, int y2, String label) {
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(x1, y1, x2, y2);
            int mx = (x1 + x2) / 2;
            int my = (y1 + y2) / 2;
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.drawString(label, mx - 12, my - 3);
        }

        private void drawGraphNode(Graphics2D g2, int x, int y, String label, Color c) {
            g2.setColor(c);
            g2.fillOval(x - 24, y - 16, 48, 32);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, x - fm.stringWidth(label) / 2, y + 4);
        }
    }

    // 4. Main Input and UI Class
    public static class UserInput {
        private JSpinner departSpinner;
        private JSpinner deadlineSpinner;
        private JSpinner budgetSpinner;
        private JSpinner maxWalkSpinner;
        private JComboBox weatherBox;
        private JComboBox priorityBox;
        private JComboBox ruleSelectorBox;
        private JCheckBox trainStatusBox;
        private JSpinner trainFareSpinner;
        private JSpinner trainTimeSpinner;
        private JSpinner trainWalkSpinner;
        private JCheckBox busStatusBox;
        private JSpinner busFareSpinner;
        private JSpinner busTimeSpinner;
        private JSpinner busWalkSpinner;
        private JCheckBox rideStatusBox;
        private JSpinner rideFareSpinner;
        private JSpinner rideTimeSpinner;
        private JSpinner rideWalkSpinner;
        // Output Views
        private JTextArea recommendationArea;
        private JTextArea truthTableArea;
        private JTextArea traversalArea;
        private VisualCanvas visualCanvas;
        // Current Evaluated State
        private CommuteOption trainOpt;
        private CommuteOption busOpt;
        private CommuteOption rideOpt;
        private CommuteOption bestOpt;
        private String currentWeather = "";
        private LocalTime deadlineTime = null;
        private double userBudget = 0.0;
        private int effectiveMaxWalk = 0;

        public void launchApp() {
            JFrame frame = new JFrame("CampusRoute - Smarter Campus Commute Planner");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 820);
            frame.setLayout(new BorderLayout(10, 10));
            // Top Toolbar
            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBorder(new EmptyBorder(10, 14, 10, 14));
            topBar.setBackground(new Color(30, 58, 138));
            JLabel title = new JLabel("CampusRoute: Explainable Campus Commute Decision App");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 17));
            topBar.add(title, BorderLayout.WEST);
            frame.add(topBar, BorderLayout.NORTH);
            // Left Panel: Inputs
            JPanel inputPanel = createInputForm();
            JScrollPane leftScroll = new JScrollPane(inputPanel);
            leftScroll.setPreferredSize(new Dimension(370, 750));
            frame.add(leftScroll, BorderLayout.WEST);
            // Right Panel: Tabbed Views
            JTabbedPane tabs = new JTabbedPane();
            // Tab 1: Propositions
            recommendationArea = createMonospaceArea();
            recommendationArea.setText("Adjust your times, fares, and preferences on the left, then click 'Evaluate Commute Options'.");
            tabs.addTab("1. Recommendation & Logic Rules", new JScrollPane(recommendationArea));
// Tab 2: Truth Table Generator (Step 5)
            JPanel ttPanel = new JPanel(new BorderLayout(5, 5));
            JPanel ttHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
            ttHeader.add(new JLabel("Select Eligibility Rule for Truth Table & AST:"));
            ruleSelectorBox = new JComboBox(new String[]{
                    "TrainEligible = T AND F_train AND D_train AND W_train",
                    "BusEligible = B AND F_bus AND D_bus AND W_bus",
                    "RideEligible = R AND F_ride AND D_ride"
            });
            ruleSelectorBox.addActionListener(e -> {
                if (trainOpt != null) {
                    ExprNode root = updateTruthTableAndAST();
                    visualCanvas.updateData(root, trainOpt, busOpt, rideOpt, bestOpt, currentWeather, deadlineTime);
                }
            });
            ttHeader.add(ruleSelectorBox);
            truthTableArea = createMonospaceArea();
            truthTableArea.setText("Truth table will generate once your commute values are evaluated.");
            ttPanel.add(ttHeader, BorderLayout.NORTH);
            ttPanel.add(new JScrollPane(truthTableArea), BorderLayout.CENTER);
            tabs.addTab("2. Truth Table & Row Explanations", ttPanel);
            // Tab 3: Expression Tree, Decision Tree & Knowledge Graph
            JPanel graphPanel = new JPanel(new BorderLayout(5, 5));
            traversalArea = new JTextArea(5, 40);
            traversalArea.setFont(new Font("Monospaced", Font.BOLD, 12));
            traversalArea.setEditable(false);
            traversalArea.setBorder(BorderFactory.createTitledBorder("Step 6: Expression Tree Traversals"));
            traversalArea.setText("Traversals will appear after evaluating your commute options.");
            visualCanvas = new VisualCanvas();
            graphPanel.add(traversalArea, BorderLayout.NORTH);
            graphPanel.add(visualCanvas, BorderLayout.CENTER);
            tabs.addTab("3. Expression Tree, Decision Tree & Knowledge Graph", graphPanel);
            frame.add(tabs, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        // Time Picker with arrows. User-Friendly ;)
        private JSpinner createTimeSpinner(int hour24, int minute) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour24);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            SpinnerDateModel model = new SpinnerDateModel(cal.getTime(), null, null, Calendar.MINUTE);
            JSpinner spinner = new JSpinner(model);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "hh:mm a");
            spinner.setEditor(editor);
            return spinner;
        }

        // Gets's time from Spineers
        private LocalTime getTimeFromSpinner(JSpinner spinner) {
            Date date = (Date) spinner.getValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
        }

        // Same thing as the time, just for the $$
        private JSpinner createMoneySpinner(double initialVal) {
            SpinnerNumberModel model = new SpinnerNumberModel(initialVal, 0.0, 200.0, 0.25);
            JSpinner spinner = new JSpinner(model);
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "$0.00");
            spinner.setEditor(editor);
            return spinner;
        }

        // Creates a Minutes Stepper
        private JSpinner createMinutesSpinner(int initialVal) {
            SpinnerNumberModel model = new SpinnerNumberModel(initialVal, 0, 180, 1);
            JSpinner spinner = new JSpinner(model);
            JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0 'min'");
            spinner.setEditor(editor);
            return spinner;
        }

        // Inputs for the Spinners (User-Friendly ;))
        private JPanel createInputForm() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(10, 12, 10, 12));
            departSpinner = createTimeSpinner(8, 0);
            deadlineSpinner = createTimeSpinner(9, 0);
            budgetSpinner = createMoneySpinner(0.0);
            maxWalkSpinner = createMinutesSpinner(0);
            weatherBox = new JComboBox(new String[]{"-- Select Weather --", "Clear", "Rain", "Storm"});
            priorityBox = new JComboBox(new String[]{"-- Select Priority --", "Lowest Cost", "Shortest Travel Time"});
            panel.add(sectionLabel("Your Commute"));
            panel.add(labeledRow("Departure Time:", departSpinner));
            panel.add(labeledRow("Arrival Deadline:", deadlineSpinner));
            panel.add(labeledRow("Available Budget:", budgetSpinner));
            panel.add(labeledRow("Max Walk Limit:", maxWalkSpinner));
            panel.add(labeledRow("Weather:", weatherBox));
            panel.add(labeledRow("Priority:", priorityBox));
            panel.add(Box.createVerticalStrut(10));
            panel.add(sectionLabel("Train Option"));
            trainStatusBox = new JCheckBox("Train Service Operating", false);
            trainFareSpinner = createMoneySpinner(0.0);
            trainTimeSpinner = createMinutesSpinner(0);
            trainWalkSpinner = createMinutesSpinner(0);
            panel.add(trainStatusBox);
            panel.add(labeledRow("Train Fare:", trainFareSpinner));
            panel.add(labeledRow("Ride Time:", trainTimeSpinner));
            panel.add(labeledRow("Walk Time:", trainWalkSpinner));
            panel.add(Box.createVerticalStrut(10));
            panel.add(sectionLabel("Bus Option"));
            busStatusBox = new JCheckBox("Bus Service Operating", false);
            busFareSpinner = createMoneySpinner(0.0);
            busTimeSpinner = createMinutesSpinner(0);
            busWalkSpinner = createMinutesSpinner(0);
            panel.add(busStatusBox);
            panel.add(labeledRow("Bus Fare:", busFareSpinner));
            panel.add(labeledRow("Ride Time:", busTimeSpinner));
            panel.add(labeledRow("Walk Time:", busWalkSpinner));
            panel.add(Box.createVerticalStrut(10));
            panel.add(sectionLabel("Backup Ride Option"));
            rideStatusBox = new JCheckBox("Backup Ride Available (R)", false);
            rideFareSpinner = createMoneySpinner(0.0);
            rideTimeSpinner = createMinutesSpinner(0);
            rideWalkSpinner = createMinutesSpinner(0);
            panel.add(rideStatusBox);
            panel.add(labeledRow("Ride Fare:", rideFareSpinner));
            panel.add(labeledRow("Ride Time:", rideTimeSpinner));
            panel.add(labeledRow("Walk Time:", rideWalkSpinner));
            panel.add(Box.createVerticalStrut(14));
            JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            JButton evalBtn = new JButton("Evaluate Commute Options");
            evalBtn.setBackground(new Color(37, 99, 235));
            evalBtn.setForeground(Color.BLACK);
            evalBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            evalBtn.addActionListener(e -> evaluateCommute());
            JButton resetBtn = new JButton("Reset");
            resetBtn.addActionListener(e -> resetForm());
            btnRow.add(evalBtn);
            btnRow.add(resetBtn);
            btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(btnRow);
            return panel;
        }

        private void resetForm() {
            departSpinner.setValue(createTimeSpinner(8, 0).getValue());
            deadlineSpinner.setValue(createTimeSpinner(9, 0).getValue());
            budgetSpinner.setValue(Double.valueOf(0.0));
            maxWalkSpinner.setValue(Integer.valueOf(0));
            weatherBox.setSelectedIndex(0);
            priorityBox.setSelectedIndex(0);
            trainStatusBox.setSelected(false);
            trainFareSpinner.setValue(Double.valueOf(0.0));
            trainTimeSpinner.setValue(Integer.valueOf(0));
            trainWalkSpinner.setValue(Integer.valueOf(0));
            busStatusBox.setSelected(false);
            busFareSpinner.setValue(Double.valueOf(0.0));
            busTimeSpinner.setValue(Integer.valueOf(0));
            busWalkSpinner.setValue(Integer.valueOf(0));
            rideStatusBox.setSelected(false);
            rideFareSpinner.setValue(Double.valueOf(0.0));
            rideTimeSpinner.setValue(Integer.valueOf(0));
            rideWalkSpinner.setValue(Integer.valueOf(0));
            trainOpt = null;
            busOpt = null;
            rideOpt = null;
            bestOpt = null;
            deadlineTime = null;
            recommendationArea.setText("Form reset. Adjust your times, fares, and preferences, then click 'Evaluate Commute Options'.");
            truthTableArea.setText("Truth table will generate once your commute values are evaluated.");
            traversalArea.setText("Traversals will appear after evaluating your commute options.");
            visualCanvas.updateData(null, null, null, null, null, "", null);
        }

        // Evaluate Propositions, Rules, Scores & Explanation
        private void evaluateCommute() {
            if (weatherBox.getSelectedIndex() == 0 || priorityBox.getSelectedIndex() == 0) {
                JOptionPane.showMessageDialog(null, "Please select both a Weather condition and a Priority preference.");
                return;
            }
            DateTimeFormatter fmt12 = DateTimeFormatter.ofPattern("h:mm ");
            LocalTime depart = getTimeFromSpinner(departSpinner);
            deadlineTime = getTimeFromSpinner(deadlineSpinner);
            userBudget = ((Number) budgetSpinner.getValue()).doubleValue();
            int rawMaxWalk = ((Number) maxWalkSpinner.getValue()).intValue();
            currentWeather = String.valueOf(weatherBox.getSelectedItem());
            String priority = String.valueOf(priorityBox.getSelectedItem());
            effectiveMaxWalk = "Storm".equals(currentWeather) ? Math.min(rawMaxWalk, 5)
                    : ("Rain".equals(currentWeather) ? Math.min(rawMaxWalk, 10) : rawMaxWalk);
            trainOpt = buildOption("Train", trainStatusBox.isSelected(), trainFareSpinner, trainTimeSpinner, trainWalkSpinner, depart, false);
            busOpt = buildOption("Bus", busStatusBox.isSelected(), busFareSpinner, busTimeSpinner, busWalkSpinner, depart, false);
            rideOpt = buildOption("Backup Ride", rideStatusBox.isSelected(), rideFareSpinner, rideTimeSpinner, rideWalkSpinner, depart, true);
            OptionList all = new OptionList();
            all.add(trainOpt);
            all.add(busOpt);
            all.add(rideOpt);
            OptionList eligibleList = new OptionList();
            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).eligible) {
                    eligibleList.add(all.get(i));
                }
            }
// Step 4: Rank eligible options by user-selected priority - User-Friendly :)
            if ("Lowest Cost".equals(priority)) {
                eligibleList.sortByCost();
            } else {
                eligibleList.sortByTime();
            }
            bestOpt = eligibleList.isEmpty() ? null : eligibleList.get(0);
// Step 9: Recommendation Report using actual user-entered values
            StringBuilder sb = new StringBuilder();
            sb.append("================================================================================\n");
            sb.append(" STEP 9: RECOMMENDATION\n");
            sb.append("================================================================================\n");
            if (bestOpt != null) {
                sb.append(String.format(" RECOMMENDED OPTION: %s\n\n", bestOpt.name.toUpperCase()));
                sb.append(String.format(
                        " %s is eligible because service is operating, the $%.2f fare is within your\n" +
                                " $%.2f budget, walking (%d min) is acceptable for %s weather (max %d min),\n" +
                                " and the estimated arrival at %s meets your %s deadline.\n",
                        bestOpt.name, bestOpt.fare, userBudget, bestOpt.walkTime,
                        currentWeather, effectiveMaxWalk, bestOpt.arrival.format(fmt12), deadlineTime.format(fmt12)
                ));
                if (eligibleList.size() > 1) {
                    sb.append(String.format("\n Tie-Breaker Applied: %d options were eligible. %s won based on '%s'.\n",
                            eligibleList.size(), bestOpt.name, priority));
                }
            } else {
                sb.append(" NO ELIGIBLE OPTION FOUND\n\n");
                sb.append(" None of your entered commute options satisfied all constraints simultaneously.\n");
            }
// Step 2: Atomic Propositions
            sb.append("\n================================================================================\n");
            sb.append(" STEP 2: PROPOSITIONS\n");
            sb.append("================================================================================\n");
            sb.append(String.format(" T       (Train operating)            = %-5b\n", trainOpt.operating));
            sb.append(String.format(" Ftrain (Train fare $%.2f <= $%.2f)    = %-5b\n", trainOpt.fare, userBudget, trainOpt.fareOk));
            sb.append(String.format(" Dtrain (Train arrival %s <= %s) = %-5b\n", trainOpt.arrival.format(fmt12), deadlineTime.format(fmt12), trainOpt.deadlineOk));
            sb.append(String.format(" Wtrain (Train walk %dm <= %dm)        = %-5b\n\n", trainOpt.walkTime, effectiveMaxWalk, trainOpt.walkOk));
            sb.append(String.format(" B       (Bus operating)              = %-5b\n", busOpt.operating));
            sb.append(String.format(" Fbus   (Bus fare $%.2f <= $%.2f)      = %-5b\n", busOpt.fare, userBudget, busOpt.fareOk));
            sb.append(String.format(" Dbus   (Bus arrival %s <= %s)   = %-5b\n", busOpt.arrival.format(fmt12), deadlineTime.format(fmt12), busOpt.deadlineOk));
            sb.append(String.format(" Wbus   (Bus walk %dm <= %dm)          = %-5b\n\n", busOpt.walkTime, effectiveMaxWalk, busOpt.walkOk));
            sb.append(String.format(" R       (Ride available)             = %-5b\n", rideOpt.operating));
            sb.append(String.format(" Fride  (Ride fare $%.2f <= $%.2f)    = %-5b\n", rideOpt.fare, userBudget, rideOpt.fareOk));
            sb.append(String.format(" Dride  (Ride arrival %s <= %s)  = %-5b\n", rideOpt.arrival.format(fmt12), deadlineTime.format(fmt12), rideOpt.deadlineOk));
// Step 3: Eligibility Rules Summary
            sb.append("\n================================================================================\n");
            sb.append(" STEP 3: CONJUNCTIVE ELIGIBILITY RULES\n");
            sb.append("================================================================================\n");
            for (int i = 0; i < all.size(); i++) {
                CommuteOption opt = all.get(i);
                sb.append(String.format(" %-12s | Fare: $%-5.2f | Total Time: %2d min | Arrival: %s | Result: %s\n",
                        opt.name, opt.fare, opt.totalTime, opt.arrival.format(fmt12),
                        opt.eligible ? "ELIGIBLE" : "INELIGIBLE (" + opt.failureReason() + ")"));
            }
            recommendationArea.setText(sb.toString());
            // Update Truth Table and Expression Tree Traversals
            ExprNode astRoot = updateTruthTableAndAST();
            // Update Visual Diagrams
            visualCanvas.updateData(astRoot, trainOpt, busOpt, rideOpt, bestOpt, currentWeather, deadlineTime);
        }

        private CommuteOption buildOption(String name, boolean operating, JSpinner fareS, JSpinner rideS,
                                          JSpinner walkS, LocalTime depart, boolean isRide) {
            double fare = ((Number) fareS.getValue()).doubleValue();
            int ride = ((Number) rideS.getValue()).intValue();
            int walk = ((Number) walkS.getValue()).intValue();
            int total = ride + walk;
            LocalTime arrival = depart.plusMinutes(total);
            boolean fareOk = fare <= userBudget;
            boolean deadlineOk = !arrival.isAfter(deadlineTime);
            boolean walkOk = isRide || walk <= effectiveMaxWalk;
            return new CommuteOption(name, operating, fare, ride, walk, total, arrival, fareOk, deadlineOk, walkOk);
        }

        // Truth Table + AST Builder & Traversals
        private ExprNode updateTruthTableAndAST() {
            int idx = ruleSelectorBox.getSelectedIndex();
            StringList vars = new StringList();
            StringList labels = new StringList();
            BoolList activeState = new BoolList();
            if (idx == 0) {
                vars.add("T");
                vars.add("Ftrain");
                vars.add("Dtrain");
                vars.add("Wtrain");
                labels.add("Train Operating");
                labels.add("Fare <= Budget");
                labels.add("Meets Deadline");
                labels.add("Walk Acceptable");
                activeState.add(trainOpt.operating);
                activeState.add(trainOpt.fareOk);
                activeState.add(trainOpt.deadlineOk);
                activeState.add(trainOpt.walkOk);
            } else if (idx == 1) {
                vars.add("B");
                vars.add("Fbus");
                vars.add("Dbus");
                vars.add("Wbus");
                labels.add("Bus Operating");
                labels.add("Fare <= Budget");
                labels.add("Meets Deadline");
                labels.add("Walk Acceptable");
                activeState.add(busOpt.operating);
                activeState.add(busOpt.fareOk);
                activeState.add(busOpt.deadlineOk);
                activeState.add(busOpt.walkOk);
            } else {
                vars.add("R");
                vars.add("Fride");
                vars.add("Dride");
                labels.add("Ride Available");
                labels.add("Fare <= Budget");
                labels.add("Meets Deadline");
                activeState.add(rideOpt.operating);
                activeState.add(rideOpt.fareOk);
                activeState.add(rideOpt.deadlineOk);
            }
            // Generate Truth Table
            StringBuilder tt = new StringBuilder();
            tt.append("Truth Table for Rule: ").append(ruleSelectorBox.getSelectedItem()).append("\n\n");
            for (int i = 0; i < vars.size(); i++) {
                tt.append(String.format("%-9s", vars.get(i)));
            }
            tt.append(String.format("%-10s %s\n", "ELIGIBLE", "ROW EXPLANATION"));
            tt.append("--------------------------------------------------------------------------------------\n");
            int n = vars.size();
            int rows = 1 << n;
            for (int r = rows - 1; r >= 0; r--) {
                boolean allTrue = true;
                boolean matchesInput = true;
                StringList failed = new StringList();
                for (int bit = 0; bit < n; bit++) {
                    boolean val = ((r >> (n - 1 - bit)) & 1) == 1;
                    if (!val) {
                        allTrue = false;
                        failed.add(labels.get(bit));
                    }
                    if (val != activeState.get(bit)) {
                        matchesInput = false;
                    }
                    tt.append(String.format("%-9s", val ? "T" : "F"));
                }
                String explanation = allTrue
                        ? "All conditions satisfied -> Option is ELIGIBLE."
                        : "Fails because " + failed.join(", ") + " is False.";
                if (matchesInput) {
                    explanation += "  <== [CURRENT INPUT]";
                }
                tt.append(String.format("%-10s %s\n", allTrue ? "TRUE" : "FALSE", explanation));
            }
            truthTableArea.setText(tt.toString());
            // Build Expression Tree & Compute Traversals
            ExprNode root = buildAST(vars);
            StringList pre = new StringList();
            StringList in = new StringList();
            StringList post = new StringList();
            preorder(root, pre);
            inorder(root, in);
            postorder(root, post);
            traversalArea.setText(
                    "Selected Rule : " + ruleSelectorBox.getSelectedItem() + "\n" +
                            "Preorder      : " + pre.join(" ") + "\n" +
                            "Inorder       : " + in.join(" ") + "\n" +
                            "Postorder     : " + post.join(" ")
            );
            return root;
        }

        //AST Construction & Recursive Traversals
        private ExprNode buildAST(StringList vars) {
            ExprNode node = new ExprNode(vars.get(0));
            for (int i = 1; i < vars.size(); i++) {
                node = new ExprNode("AND", node, new ExprNode(vars.get(i)));
            }
            return node;
        }

        private void preorder(ExprNode n, StringList out) {
            if (n == null) return;
            out.add(n.val);
            preorder(n.left, out);
            preorder(n.right, out);
        }

        private void inorder(ExprNode n, StringList out) {
            if (n == null) return;
            if (n.left != null) out.add("(");
            inorder(n.left, out);
            out.add(n.val);
            inorder(n.right, out);
            if (n.right != null) out.add(")");
        }

        private void postorder(ExprNode n, StringList out) {
            if (n == null) return;
            postorder(n.left, out);
            postorder(n.right, out);
            out.add(n.val);
        }

        // UI Helpers
        private JLabel sectionLabel(String text) {
            JLabel lbl = new JLabel(text);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl.setForeground(new Color(30, 58, 138));
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            return lbl;
        }

        private JPanel labeledRow(String label, JComponent comp) {
            JPanel row = new JPanel(new BorderLayout(5, 2));
            row.setMaximumSize(new Dimension(345, 28));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lbl = new JLabel(label);
            lbl.setPreferredSize(new Dimension(140, 24));
            row.add(lbl, BorderLayout.WEST);
            row.add(comp, BorderLayout.CENTER);
            return row;
        }

        private JTextArea createMonospaceArea() {
            JTextArea area = new JTextArea();
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            area.setEditable(false);
            area.setMargin(new Insets(10, 10, 10, 10));
            return area;
        }
    }
}
