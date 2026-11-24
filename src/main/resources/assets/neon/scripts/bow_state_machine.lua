local base = require('tacz_default_state_machine')
local idle = setmetatable({}, { __index = base.main_track_states.idle })
local states = setmetatable({ idle = idle }, { __index = base.main_track_states })
local bow = setmetatable({ main_track_states = states }, { __index = base })

-- Keep the draw on its own track so that reload, inspect and holster retain their native transitions.
local function stringTrack(context)
    return context:getTrack(base.STATIC_TRACK_LINE, base.PARALLEL_TRACK_1)
end

function idle.update(this, context)
    local track = stringTrack(context)
    if context:isCharging() and context:getChargeProgress() > 0 then
        if not this.stringActive then
            context:runAnimation('string_draw', track, false, PLAY_ONCE_HOLD, 0)
            this.stringActive = true
        end
        context:setAnimationProgress(track, context:getChargeProgress(), true)
    elseif this.stringActive then
        context:stopAnimation(track)
        this.stringActive = false
    end
end

function idle.transition(this, context, input)
    if this.stringActive and (input == INPUT_SHOOT or input == INPUT_PUT_AWAY or input == INPUT_RELOAD or input == INPUT_INSPECT) then
        context:stopAnimation(stringTrack(context))
        this.stringActive = false
    end
    return base.main_track_states.idle.transition(this, context, input)
end

function bow:initialize(context)
    self.stringActive = false
    base.initialize(self, context)
end

return bow
