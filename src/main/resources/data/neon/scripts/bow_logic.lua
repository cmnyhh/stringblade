local bow = {}

function bow.shoot(api)
    local draw = api:getChargeRatio()
    api:setShotDamageMultiplier(0.35 + 0.65 * draw)
    api:setProjectileSpeedMultiplier(0.45 + 0.55 * draw)
    api:shootOnce(api:isShootingNeedConsumeAmmo())
end

return bow
