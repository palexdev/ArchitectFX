
<h1 align="center">
  ArchitectFX
</h1>

<h2 align="center" style="font-style: italic">
  One step closer to the first milestone!
</h2>

![Imgur](https://imgur.com/7Cgdznu.png)

<br>

ArchitectFX is the "wannabe" nextgen successor of SceneBuilder.

_Q: What do you mean by "wannabe¨?_  

Currently, the project is in the early stages of development. My focus is on understanding and experimenting on how to do certain things, rather than trying to code definitive APIs, algorithms, classes...

The ultimate goal would be to offer the community a modern tool that can completely replace SceneBuilder. A tool that does not look like it's straight out of the nineties. A tool that does not discriminate, that offers equal support for JavaFX as well as third-party libraries. A tool built for the user, as customizable as possible.


### **Important Note!**

There is a crucial difference between ArchitectFX and SceneBuilder.  
I absolutely **despise** XML in any of its forms.
I know that XML is technically better for tree structures, but I don't care one bit. I really can't stand its syntax. Not only that, I think XML parsing is stupidly complicated, probably because it's **old**.

Which is why this project currently uses YAML instead (I also did try TOML and I quite like it but [this](https://github.com/toml-lang/toml/pull/904) feature is critial but not yet available).  
At this stage, the format is evolving constantly.

After quite some time, I decided to name my format JDSL (with extension .jdsl) which stands for
Java Deserialization Language (yeah it's the best I came up with, don't judge lol).  
For now, documentation is not really available because I don't know yet what would be the best way to document a file
format such as this, and also because soon there should be a JSON schema available for it at
[SchemaStore](https://schemastore.org).

## Current Goals

In terms of functionality, the current goal is not to create an exact copy of SceneBuilder...yet.  
I'm aiming at something easier and quicker to implement, a mode that I call "Split View" (Haven't decided on the final name though).  
This mode lets you open a document, builds the Scene by reading it, and would allow you to refresh the view whenever the document is modified.
In other words, this mode does not let you modify the Scene by dragging controls, or changing their properties with an inspector. You have to do it in the document.

This mode's con is that compared to a GUI editor it's more inconvenient. But! The pro is that you still have a live preview of what you view will look like once loaded.
## Project Status

I figured that _maybe_ organizing the progress in a Kanban board would be definitely better than having a checklist here in Markdown. So, you can monitor the progress [here](https://trello.com/b/v303w5k6/architectfx-progress).