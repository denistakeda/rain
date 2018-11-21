# rain

This repo is a test assignment done for Snapview company

## Installation

1. [Install Clojure](https://www.clojure.org/guides/getting_started)
2. [Install Lein](https://leiningen.org/#install)
3. `lein clean && lein install`
4. `lein run`

## Algorithms description

There are some important points we need to consider before starting with algorithm:
1. It doesn't matter in which order you put water on the columns
2. It doesn't matter to which column on the plain you put water. Water behaves equally for any.
3. There are only 3 possible "shapes" near one point:
  - `glass` plane, surrounded with walls
  - `slope` wall on the one side and hollow on the other
  - `hill` hollows on the both sides
4. Every shape eventually becomes `glass`
5. Water behaves differently on different shapes:
  - `glass` water fulfill glass equally in each point
  - `slope` put the water on the slope shape is the same as put this water on the bottom of this slope
  - `hill` put the water on the hill is the same as split water by half and put each half in both sides
6. There is some minimum amount of water, that change shape for at least one place

### Algorithm
Moving from left to right put the required amount of water on the each column.
1. Get the `minimum amount of water to change shape`:
  - For `glass` it's height of lowest wall times width of the plane
  - For `slope` minimum amount is the same as minimum amount for its bottom
  - For `hill` it's minimum of 3 values, amount of water divided by two, minimum to the left, minimum to the right
2. Put the minimum amount of water on point
  - For `glass` water fulfill it equally by the whole width
  - For `slope` put the water on the bottom of the slope (and continue recursively)
  - For `hill` put the water on both slopes (and continue recursively)
3. Calculate the new landscape after putting minimum amount of water
4. If there is some water left, start from 1 with a new landscape
5. After putting required amount of water on each column, we have a new landscape. To calculate water level just subtract old landscape from the new one

## Correctness proof

We can use loop invariant to proof this algorithm

### Initialization
For amount of columns equal to 1 the system is already in ballance

### Maintenance
Given that after n iterations water is balanced correctly. Proof that after n + 1 iterations it's still balanced.

When we put water on column it behaves differently for different shape.
1. When we put some water inside `glass` there are two options:
  - The amount of water is not enough to fill the glass. In that case water spread equally on the bottom of the glass and system is still balanced.
  - The amount of water is equal or bigger than the amount required to fill the glass. In that case we put exact amount of water required to fill the glass. After that its not glass any more. We can recalculate the shape and continue with the rest of the water. After that water is balanced.
2. When we put some water on the `slope` the water goes down the slope and this is equal to put this water at the bottom of the slope. We can do the same calculation for this new point which balances water.
3. When we put some water on the `hill` shape the water goes down the both slopes and this is equal to split water by two and put both halfs at the bottom to the left and to the right. After that we can balance water in this new points. After that water is balanced.
When water goes down the slope it can go in only one direction, because water can't go up. But if it moves one direction sooner or later if meets the new wall or the end (which is infinite wall). This last shape is always `glass` and we already proved correctness for glass shape.

### Termination
We have only finite amount of water to put in the system and hence finite amount of iterations. After the last iteration the water is fully balanced and algorithm is finished.

