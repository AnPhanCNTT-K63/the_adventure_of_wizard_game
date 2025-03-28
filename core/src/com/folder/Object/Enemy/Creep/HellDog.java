package com.folder.Object.Enemy.Creep;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.folder.Boot;
import com.folder.Object.Enemy.Enemy;
import com.folder.Object.MagicEffect.Effect.BleedingEffect;
import com.folder.Object.MagicEffect.MagicEffect;
import com.folder.Object.MainCharacter;
import com.folder.Screen.GameScreen;
import com.folder.UI.Status;

import java.util.LinkedList;

public class HellDog extends Enemy {
    enum STATE {WALK, ATTACK, RUN, HURT, DEAD}

    private STATE currentState;
    private STATE previousState;

    private LinkedList<Fixture> fixtures;


    private Animation<TextureRegion> Walk;
    private Animation<TextureRegion> Chase;
    private Animation<TextureRegion> Attack;
    private Animation<TextureRegion> Hurt;
    private Animation<TextureRegion> Dead;

    private float stateTime;
    private float actionDuration;

    private boolean isWalking;
    private boolean isAttacking;
    private boolean isChasing;
    private boolean canMove;
    private boolean isDead;
    private boolean setToDead;
    private boolean isExist;
    private boolean isCreateFixture;
    private boolean isHurt;
    private boolean isBleeding;
    private boolean isHeavyHurt;
    private boolean isReverse;


    private boolean isTurningRight;

    private MagicEffect bleedingEffect;

    private int heart;

    public HellDog(GameScreen screen, float posX, float posY) {
        super(screen, posX, posY);
        fixtures = new LinkedList<>();

        currentState = previousState = STATE.WALK;

        isDead = false;
        isWalking = true;
        isAttacking = false;
        isChasing = false;
        isHurt = false;
        isHeavyHurt = false;
        canMove = true;
        isBleeding = false;
        setToDead = false;
        isExist = true;
        isCreateFixture = true;
        isReverse = false;

        isTurningRight = true;

        stateTime = 0;
        actionDuration = 0;

        bleedingEffect = new BleedingEffect(screen);
        heart = 10;

        Array<TextureRegion> frames = new Array<TextureRegion>();

        for (int i = 0; i < 9; i++)
            frames.add(new TextureRegion(screen.getHellDogAtlas().findRegion("Walk"), i * 128, 0, 128, 128));
        Walk = new Animation<TextureRegion>(1 / 11f, frames);
        frames.clear();

        for (int i = 0; i < 6; i++)
            frames.add(new TextureRegion(screen.getHellDogAtlas().findRegion("Run"), i * 128, 0, 128, 128));
        Chase = new Animation<TextureRegion>(1 / 9f, frames);
        frames.clear();

        for (int i = 0; i < 6; i++)
            frames.add(new TextureRegion(screen.getHellDogAtlas().findRegion("Attack1"), i * 128, 0, 128, 128));
        Attack = new Animation<TextureRegion>(1 / 8f, frames);
        frames.clear();

        for (int i = 0; i < 3; i++)
            frames.add(new TextureRegion(screen.getHellDogAtlas().findRegion("Hurt"), i * 128, 0, 128, 128));
        Hurt = new Animation<TextureRegion>(1 / 3f, frames);
        frames.clear();

        for (int i = 0; i < 5; i++)
            frames.add(new TextureRegion(screen.getHellDogAtlas().findRegion("Dead"), i * 128, 0, 128, 128));
        Dead = new Animation<TextureRegion>(0.5f, frames);
        frames.clear();
    }

    @Override
    public void setUpBody() {
        setBounds(getX(), getY(), 128 / Boot.PPM, 128 / Boot.PPM);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(getWidth() / 2 - 20 / Boot.PPM, getHeight() / 2 - 28 / Boot.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = Boot.ENEMY_BIT;
        fixtureDef.filter.maskBits = Boot.GROUND_BIT | Boot.ATTACK_BIT | Boot.CHARACTER_BIT | Boot.WALL_BIT;

        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    public TextureRegion getStatus(float deltaTime) {
        currentState = getState();
        TextureRegion region;

        switch (currentState) {
            case RUN:
                region = Chase.getKeyFrame(stateTime, true);
                break;
            case ATTACK:
                region = Attack.getKeyFrame(stateTime, true);
                break;
            case HURT:
                region = Hurt.getKeyFrame(stateTime, true);
                break;
            case DEAD:
                region = Dead.getKeyFrame(stateTime);
                break;
            default:
                region = Walk.getKeyFrame(stateTime, true);
                break;
        }

        if (!isTurningRight && !region.isFlipX()) region.flip(true, false);
        else if (isTurningRight && region.isFlipX()) region.flip(true, false);

        stateTime = currentState == previousState ? stateTime + deltaTime : 0;
        previousState = currentState;

        return region;
    }

    public STATE getState() {
        if (isChasing) return STATE.RUN;
        else if (isAttacking) return STATE.ATTACK;
        else if (isHeavyHurt) return STATE.HURT;
        else if (isDead) return STATE.DEAD;
        else return STATE.WALK;
    }

    public void action() {
        setActionDuration();

        if (isDead && stateTime >= 2f) isExist = false;

        if (isHurt) {
            heart--;
            if (heart == 5) isHeavyHurt = true;
            if (heart == 0) setToDead = true;
            isHurt = false;
        }

        if (isDead || isHeavyHurt) {
            isChasing = false;
            isAttacking = false;
            canMove = false;
        }

        destroyBody();

        if (!isDead) {
            if (!isHeavyHurt) {
                if (body.getPosition().x - MainCharacter.body.getPosition().x < 70 / Boot.PPM && body.getPosition().x - MainCharacter.body.getPosition().x > 10 / Boot.PPM && !MainCharacter.isDead) {
                    isAttacking = true;
                    isTurningRight = false;
                } else if (body.getPosition().x - MainCharacter.body.getPosition().x > -70 / Boot.PPM && body.getPosition().x - MainCharacter.body.getPosition().x < 10 / Boot.PPM && !MainCharacter.isDead) {
                    isAttacking = true;
                    isTurningRight = true;
                } else {
                    canMove = true;
                }

                if (isAttacking) {
                    isChasing = false;
                    canMove = false;
                }

                if (!isAttacking)
                    if (body.getPosition().x - MainCharacter.body.getPosition().x < 300 / Boot.PPM && body.getPosition().x - MainCharacter.body.getPosition().x > 30 / Boot.PPM && !MainCharacter.isDead) {
                        isChasing = true;
                        isTurningRight = false;
                        velocity.set(-2.3f, 0);
                    } else if (body.getPosition().x - MainCharacter.body.getPosition().x > -300 / Boot.PPM && body.getPosition().x - MainCharacter.body.getPosition().x < 10 / Boot.PPM && !MainCharacter.isDead) {
                        isChasing = true;
                        isTurningRight = true;
                        velocity.set(2.3f, 0);
                    } else
                        isChasing = false;

                isWalking = !isChasing;

                if (isWalking && stateTime >= 3.5f) {
                    isReverse = true;
                    stateTime = 0;
                }

                if (isReverse) {
                    isTurningRight = !isTurningRight;
                    isReverse = false;
                }

                if (canMove) {
                    if (isWalking) if (isTurningRight) {
                        velocity.set(1.3f, 0);
                    } else velocity.set(-1.3f, 0);
                } else velocity.set(0, 0);

                velocity.y = body.getLinearVelocity().y;
                body.setLinearVelocity(velocity);

            }
        }
        if (isAttacking) setAttackBound();
        timeToDestroyFixture();
    }

    public void createFixture(float time, float width, float height, float x, float y, float angle, short categoryBit, short maskBit) {
        if (isCreateFixture && actionDuration >= time) {
            PolygonShape shape = new PolygonShape();

            if (isTurningRight)
                shape.setAsBox(width / Boot.PPM, height / Boot.PPM, new Vector2(x / Boot.PPM, y / Boot.PPM), angle);
            else shape.setAsBox(width / Boot.PPM, height / Boot.PPM, new Vector2(-x / Boot.PPM, y / Boot.PPM), angle);

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = categoryBit;
            fixtureDef.filter.maskBits = maskBit;
            fixtureDef.isSensor = true;

            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(this);
            fixtures.add(fixture);

            shape.dispose();

            isCreateFixture = false;
        }
    }

    public void timeToDestroyFixture() {
        setTimeToDestroyFixture(isAttacking, 0.78f);
    }

    public void setTimeToDestroyFixture(boolean action, float time) {
        if (action) {
            if (actionDuration >= time) {
                isCreateFixture = true;
                destroyFixture();
            }
        }
    }

    public void destroyFixture() {
        for (Fixture fixture : fixtures)
            if (fixture.isSensor()) body.destroyFixture(fixture);
        fixtures.clear();
    }

    public void setAttackBound() {
        createFixture(0.38f, 25, 25, 40, 4, 0, Boot.ENEMY_ATTACK_BIT, Boot.CHARACTER_BIT);
    }

    @Override
    public void update(float deltaTime) {
        action();
        if (isBleeding) bleedingEffect.update(deltaTime, body.getPosition().x, body.getPosition().y);
        setBounds(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 + 28 / Boot.PPM, 128 / Boot.PPM, 128 / Boot.PPM);
        setRegion(getStatus(deltaTime));
    }


    @Override
    public void beDamaged() {
        isHurt = true;
        isBleeding = true;
    }

    public void setActionDuration() {
        if (isHeavyHurt) {
            if (actionDuration >= 1.5f) {
                isHeavyHurt = false;
                actionDuration = 0;
            }
            actionDuration += Gdx.graphics.getDeltaTime();
        }

        if (isAttacking) {
            if (actionDuration >= 0.78f) {
                isAttacking = false;
                actionDuration = 0;
            }
            actionDuration += Gdx.graphics.getDeltaTime();
        }

        if (isBleeding) {
            if (bleedingEffect.getActionDuration() >= 0.25f) {
                isBleeding = false;
                bleedingEffect.setActionDuration(0);
            }
            bleedingEffect.updateActionDuration(Gdx.graphics.getDeltaTime());
        }
    }

    public void destroyBody() {
        if (setToDead && !isDead) {
            world.destroyBody(body);
            isDead = true;
            Status.addScore(50);
        }
    }

    @Override
    public void destroyWhenNextMap() {
        if (!isDead) {
            world.destroyBody(body);
            isDead = true;
        }
    }

    @Override
    public void draw(Batch batch) {
        if (isExist) super.draw(batch);
        if (isBleeding) bleedingEffect.draw(batch);
    }

    @Override
    public void reverseVelocity() {
        isReverse = true;
    }

}
