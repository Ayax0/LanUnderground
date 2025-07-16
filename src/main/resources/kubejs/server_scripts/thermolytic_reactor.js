ServerEvents.recipes(event => {
  event.custom({
    type: 'modern_industrialization:thermolytic_reactor',
    eu: 2,
    duration: 240,
    item_inputs: [],
    fluid_inputs: [
      { fluid: 'minecraft:water', amount: 1000 },
    ],
    item_outputs: [],
    fluid_outputs: [
      { fluid: 'modern_industrialization:oxygen', amount: 400 }
    ],
  });

  event.custom({
    type: 'modern_industrialization:thermolytic_reactor',
    eu: 2,
    duration: 240,
    item_inputs: [
      { item: 'minecraft:raw_iron', amount: 1 },
    ],
    item_outputs: [
      { item: 'minecraft:iron_ingot', amount: 1 }
    ],
    fluid_outputs: [
      { fluid: 'modern_industrialization:oxygen', amount: 100 }
    ],
  });

  event.custom({
    type: 'modern_industrialization:thermolytic_reactor',
    eu: 2,
    duration: 240,
    item_inputs: [
      { item: 'minecraft:raw_copper', amount: 1 },
    ],
    item_outputs: [
      { item: 'minecraft:copper_ingot', amount: 1 }
    ],
    fluid_outputs: [
      { fluid: 'modern_industrialization:oxygen', amount: 100 }
    ],
  });

  event.custom({
    type: 'modern_industrialization:thermolytic_reactor',
    eu: 2,
    duration: 240,
    item_inputs: [
      { item: 'minecraft:raw_gold', amount: 1 },
    ],
    item_outputs: [
      { item: 'minecraft:gold_ingot', amount: 1 }
    ],
    fluid_outputs: [
      { fluid: 'modern_industrialization:oxygen', amount: 100 }
    ],
  });

  event.custom({
    type: 'modern_industrialization:thermolytic_reactor',
    eu: 2,
    duration: 240,
    item_inputs: [
      { item: 'minecraft:sand', amount: 8 },
    ],
    item_outputs: [
      { item: 'modern_industrialization:silicon_dust', amount: 1 }
    ],
    fluid_outputs: [
      { fluid: 'modern_industrialization:oxygen', amount: 50 }
    ],
  });
})
