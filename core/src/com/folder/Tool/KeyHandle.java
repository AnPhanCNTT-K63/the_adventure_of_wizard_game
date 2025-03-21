package com.folder.Tool;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;


import com.folder.Object.MainCharacter;

public class KeyUpHandle extends InputAdapter {


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.L)
            MenuHandle.isMenu = true;

        if (keycode == Input.Keys.ESCAPE && MenuHandle.isMenu) {
            MenuHandle.escape = true;
            MenuHandle.isMenu = false;
        }


        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A || keycode == Input.Keys.D) {
            MainCharacter.isMoving = false;
            MainCharacter.isAllowedMove = true;
        }

        if (keycode == Input.Keys.SPACE && MainCharacter.isJumping) {
            MainCharacter.isFalling = true;
            MainCharacter.isJumping = false;
            MainCharacter.isAllowedJump = true;
            //MainCharacter.jumpDistance = 0;
        }

        if (keycode == Input.Keys.W || keycode == Input.Keys.S) {
            MainCharacter.isLooking = false;
            MainCharacter.isLookingUp = false;
            MainCharacter.isReturn = true;
        }

        if (keycode == Input.Keys.P) {
            MainCharacter.isClimbing = false;
        }

        return true;
    }

}
