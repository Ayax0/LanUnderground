ServerEvents.recipes(event => {
  event.custom({
    type: "modern_industrialization:atmosphere_generator",
    eu: 2,
    duration: 1200,
    fluid_inputs: [{ fluid: "modern_industrialization:oxygen", amount: 1000 }],
    fluid_outputs: [{ fluid: "modern_industrialization:oxygen", amount: 0 }],
  });
})
