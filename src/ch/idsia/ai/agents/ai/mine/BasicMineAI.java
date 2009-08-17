package ch.idsia.ai.agents.ai.mine;
import ch.idsia.ai.agents.*;
import ch.idsia.mario.environments.Environment;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.utils.MathX;


public class BasicMineAI extends RegisterableAgent implements Agent
{
    boolean nextTry;
    boolean hasToJump=false;
    int trueJumpCounter;
    boolean isNotSafe=false;
    public BasicMineAI()
    {
        super("BasicMineAI");
    }

    @Override
     public void reset() {
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
        trueJumpCounter = 0;
    }
    private byte[][] decode(String estate)
    {
        byte[][] dstate = new byte[Environment.HalfObsWidth*2][Environment.HalfObsHeight*2];
        for (int i = 0; i < dstate.length; ++i)
            for (int j = 0; j < dstate[0].length; ++j)
                dstate[i][j] = 2;
        int row = 0;
        int col = 0;
        int totalBitsDecoded = 0;
        for (int i = 0; i < estate.length(); ++i)
        {
            char cur_char = estate.charAt(i);
            for (int j = 0; j < 8; ++j)
            {
                totalBitsDecoded++;
                if (col > Environment.HalfObsHeight*2 - 1)
                {
                    ++row;
                    col = 0;
                }

                if ((MathX.powsof2[j] & cur_char) != 0)
                {

                    try{
                        dstate[row][col] = 1;
                    }
                    catch (Exception e)
                    {
                     //   System.out.println("row = " + row);
                     //   System.out.println("col = " + col);
                    }
                }
                else
                {
                    dstate[row][col] = 0; //TODO: Simplify in one line of code.
                }
                ++col;
                if (totalBitsDecoded == 484)
                    break;
            }
        }

        //System.out.println("\ntotalBitsDecoded = " + totalBitsDecoded);
        return dstate;
    }
    boolean darBaja=false;

    private int isEnemyDanger(byte[][] grid)
    {
        // [11][11] soy yo
        for(int x=11;x<17;x++)
        {
            for(int y=11;y<13;y++)
            {
                if(!(x==11 && y==11)&&(grid[x][y]>0)&&(grid[x][y]<14))
                    return grid[x][y];
            }
        }
        return 0;
    }
    
    public boolean[] getAction(Environment observation) {
        byte[][] levelScene = observation.getLevelSceneObservation(/*1*/);
        int modo = observation.getMarioMode(); //0 chico 1 grande 2 fuego        
        
        /*byte[][] enemiesFromBitmap = observation.getEnemiesObservation();

        for (int i = 0; i < enemiesFromBitmap.length; ++i)
        {
            for (int j = 0; j < enemiesFromBitmap[0].length; ++j)
            {
                
                    System.out.print(enemiesFromBitmap[i][j] + " ");
                
            }
            System.out.println("");
        }*/
        if(darBaja)
        {
            darBaja=false;
            action[Mario.KEY_SPEED]=true;
        }
        byte[][] enemyGrid = observation.getEnemiesObservation();
        int enemy;
        enemy = this.isEnemyDanger(enemyGrid);
        if(modo==2)
        {                
            if(enemy!=0)
            {
                action[Mario.KEY_SPEED] =false;
                darBaja=true;
            }
        }
        if(enemy>0)
        {
            action[Mario.KEY_RIGHT]=false;
            action[Mario.KEY_LEFT]=true;
            hasToJump=true;
            isNotSafe=true;
        }else
        {
            if((trueJumpCounter>0)&& isNotSafe)
            {
                hasToJump=false;
                isNotSafe=false;
                action[Mario.KEY_JUMP]=false;
                action[Mario.KEY_RIGHT]=true;
                action[Mario.KEY_LEFT]=false;
            }
            
        }
        if(observation.isMarioOnGround() && trueJumpCounter>7 && action[Mario.KEY_JUMP]==true)
        {
            action[Mario.KEY_JUMP]=false;
            trueJumpCounter=0;
        }
        if (levelScene[11][12] != 0 ||
            levelScene[12][12] == 0 ||
            //levelScene[12][12] ==-10||
            hasToJump)
        {
            if (observation.mayMarioJump() || 
                ( !observation.isMarioOnGround() && action[Mario.KEY_JUMP]) ||
                hasToJump)
            {         
                action[Mario.KEY_JUMP] = true;
            }
            ++trueJumpCounter;
        }
        else
        {
            action[Mario.KEY_JUMP] = false;
            trueJumpCounter = 0;            
        }

        if (trueJumpCounter > 46)
        {
            trueJumpCounter = 0;
            action[Mario.KEY_JUMP] = false;
        }

        return action;
    }
    

}
