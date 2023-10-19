# NeuralNetInJava

I wrote this Java project in 2021 (First year of Uni) with the hopes of creating a Neural Network to play Snake by only using the jCuda library.
I implemented an evolutionary model to train the networks with the goal of getting the highest score in Snake. 

In the end, I saw some success with marginal increases in the score as the neural networks trained, however, I was unable to create a model that was better than an average human player.

## What I learned
1. Low level programming is hard! 

It was my first time writing code so close to hardware (this was before I learned C in school), so I often had memory leak issues in vram, and I frequently had trouble with how the matrices were organized in memory.

2. Neural networks can be represented by matrix operations.

Originally, I had thought that neural networks were difficult and unwieldly to represent, however, due to how the outputs are calculated, the weights and biases can be represented by matrices, and simply added and multiplied together to compute the output.

## Conclusion
While this was a fun project, I was disappointed with the results. I believe that my lack of knowledge of the evolutionary neural network model contributed to the lackluster performance of the system, even after many generations. 
However, I learned a great deal about cuda programming and BLAS standards, which I think makes this project worthwhile. 
In the future, I may revisit this project again, to hopefully create an evolutionary algorithm that can create results better than a human player. 
