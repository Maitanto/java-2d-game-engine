package engine.graphics.hud;

public interface IHealthBar {

	public int getCurrentHP();

	public int getTotalHP();

	public void setCurrentHP(int hp);

	public void setTotalHP(int hp);
}
