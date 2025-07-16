let THERMOLYTIC_REACTOR;

MIMachineEvents.registerRecipeTypes(event => {
    THERMOLYTIC_REACTOR = event.register("thermolytic_reactor")
        .withFluidInputs()
        .withItemInputs()
        .withFluidOutputs()
        .withItemOutputs();
});

MIMachineEvents.registerMachines(event => {
  event.craftingSingleBlock(
      /* GENERAL PARAMETERS FIRST */
      // English name, internal name, recipe type (see above), list of tiers (can be bronze/steel/electric)
      "Thermolytic Reactor", "thermolytic_reactor", THERMOLYTIC_REACTOR, ["bronze", "steel", "electric"],
      /* GUI CONFIGURATION */
      // Background height (or -1 for default value), progress bar, efficiency bar, energy bar
      180, event.progressBar(90, 35, "furnace"), event.efficiencyBar(40, 80), event.energyBar(10, 34),
      /* SLOT CONFIGURATION */
      // Number of slots: item inputs, item outputs, fluid inputs, fluid outputs
      1, 1, 1, 1,
      // Capacity for fluid slots (unused here)
      16,
      // Slot positions: items and fluids.
      // Explanation: 3x3 grid of item slots starting at position (42, 27), then 1x3 grid of item slots starting at position (139, 27).
      items => items.addSlots(40, 35, 1, 1).addSlots(150, 35, 1, 1), fluids => fluids.addSlot(40, 55).addSlot(150, 55),
      /* MODEL CONFIGURATION */
      // front overlay?, top overlay?, side overlay?
      true, false, true,
  );
})
