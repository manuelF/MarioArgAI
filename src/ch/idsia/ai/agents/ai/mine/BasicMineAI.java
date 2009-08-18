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
    boolean flee=false;
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
   
    boolean darBaja=false;

    private int isEnemyDanger(byte[][] grid)
    {
        // [11][11] soy yo
        for(int x=11;x<19;x++)
        {
            for(int y=11;y<13;y++)
            {
                if(!(x==11 && y==11)&&(grid[x][y]>0)&&(grid[x][y]<13))
                    return grid[x][y];
            }
        }
        return 0;
    }

    private int distanceToEnemyDanger(byte[][] grid)
    {
        for(int x=11;x<19;x++)
        {
            for(int y=11;y<13;y++)
            {
                if(!(x==11 && y==11)&&(grid[x][y]>0)&&(grid[x][y]<13))
                    return (x-11+y-11);
            }
        }
        return 0;
    }

    public boolean[] getAction(Environment observation) {
        byte[][] levelScene = observation.getLevelSceneObservation(/*1*/);
        int modo = observation.getMarioMode(); //0 chico 1 grande 2 fuego        

         byte[][] enemiesFromBitmap = observation.getEnemiesObservation();



        if(darBaja)
        {
            darBaja=false;
            action[Mario.KEY_SPEED]=true;
        }
        byte[][] enemyGrid = observation.getEnemiesObservation();
        int enemy; //que enemigo es?
        enemy = this.isEnemyDanger(enemyGrid);
        if(modo==2) //fueguito
        {                
            if(enemy!=0)
            {
                action[Mario.KEY_SPEED] =false;
                darBaja=true; //flag para prender de vuelta la velocidad en
                              //el turno siguiente
            }
        }
        if(enemy>0)
        {
            if(enemy!=9) //no estompeamos a spiky, rajemos
            {
                if(distanceToEnemyDanger(enemyGrid)<3) //hardcode, pero stompea bien
                {
                    flee=true;
                    action[Mario.KEY_RIGHT]=false;
                    action[Mario.KEY_LEFT]=true;
                }
            }
            hasToJump=true;
            isNotSafe=true;
        }else
        {
            if((trueJumpCounter>0)&& isNotSafe)
            {
                if(flee)
                {
                    action[Mario.KEY_RIGHT]=true;
                    action[Mario.KEY_LEFT]=false;
                    flee=false;
                }
                hasToJump=false;
                isNotSafe=false;
                action[Mario.KEY_JUMP]=false;
                
            }
            
        }
        if(observation.isMarioOnGround() && trueJumpCounter>7 && action[Mario.KEY_JUMP]==true)
        {
            action[Mario.KEY_JUMP]=false;
            trueJumpCounter=0;
        }
        if (levelScene[11][12] != 0 ||
            levelScene[12][12] == 0 ||
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
