public class Session {
    private double avgReactionTime;
    private double bestReactionTime;
    private double improvement;
    private int mistakes;
    private Date sessionDate;

    public Session() {
        this.avgReactionTime = 0.0;
        this.bestReactionTime = 0.0;
        this.improvement = 0.0;
        this.mistakes = 0;
        this.sessionDate = new Date();
    }

    public Session(double avgReactionTime, double bestReactionTime, double improvement, int mistakes, Date sessionDate) {
        this.avgReactionTime = avgReactionTime;
        this.bestReactionTime = bestReactionTime;
        this.improvement = improvement;
        this.mistakes = mistakes;
        this.sessionDate = sessionDate;
    }

    // Getters and Setters
    public double getAvgReactionTime() {
        return avgReactionTime;
    }

    public void setAvgReactionTime(double avgReactionTime) {
        this.avgReactionTime = avgReactionTime;
    }

    public double getBestReactionTime() {
        return bestReactionTime;
    }

    public void setBestReactionTime(double bestReactionTime) {
        this.bestReactionTime = bestReactionTime;
    }

    public double getImprovement() {
        return improvement;
    }

    public void setImprovement(double improvement) {
        this.improvement = improvement;
    }

    public int getMistakes() {
        return mistakes;
    }

    public void setMistakes(int mistakes) {
        this.mistakes = mistakes;
    }

    public Date getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(Date sessionDate) {
        this.sessionDate = sessionDate;
    }
}
