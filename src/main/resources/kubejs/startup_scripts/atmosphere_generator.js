let ATMOSPHERE_GENERATOR;

MIMachineEvents.registerRecipeTypes(event => {
    ATMOSPHERE_GENERATOR = event.register("atmosphere_generator")
        .withFluidInputs()
        .withFluidOutputs();
});

MIMachineEvents.registerMachines(event => {
  event.craftingSingleBlock(
      "Atmosphere Generator", "atmosphere_generator", ATMOSPHERE_GENERATOR, ["bronze", "steel", "electric"],
      /* GUI CONFIGURATION */
      // Background height (or -1 for default value), progress bar, efficiency bar, energy bar
      185, event.progressBar(90, 33, "centrifuge"), event.efficiencyBar(40, 85), event.energyBar(10, 34),
      /* SLOT CONFIGURATION */
      // Number of slots: item inputs, item outputs, fluid inputs, fluid outputs
      1, 0, 1, 1,
      // Capacity for fluid slots (unused here)
      16,
      // Slot positions: items and fluids.
      // Explanation: 3x3 grid of item slots starting at position (42, 27), then 1x3 grid of item slots starting at position (139, 27).
      items => items.addSlots(92, 60, 1, 1), fluids => fluids.addSlot(40, 35).addSlot(150, 35),
      /* MODEL CONFIGURATION */
      // front overlay?, top overlay?, side overlay?
      true, true, true,
      /* MACHINE CONFIGURATION */
      config => config.steamCustomOverclock({})
  );
})

