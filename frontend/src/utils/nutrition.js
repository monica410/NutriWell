export const NUTRIENTS = [
  { key: 'caloriesKcal', label: 'Calories', unit: 'kcal' },
  { key: 'proteinG', label: 'Protein', unit: 'g' },
  { key: 'carbohydratesG', label: 'Carbs', unit: 'g' },
  { key: 'totalFatG', label: 'Fat', unit: 'g' },
  { key: 'saturatedFatG', label: 'Saturated fat', unit: 'g' },
  { key: 'fiberG', label: 'Fiber', unit: 'g' },
  { key: 'totalSugarG', label: 'Sugar', unit: 'g' },
  { key: 'sodiumMg', label: 'Sodium', unit: 'mg' },
]

// Sum available values, skipping missing nutrients in individual food entries.
export function sumNutrition(foods) {
  return Object.fromEntries(NUTRIENTS.map(({ key }) => {
    let total = 0
    for (const food of foods) {
      const value = food.nutrition?.[key]
      if (Number.isFinite(value)) total += value
    }
    return [key, total]
  }))
}
