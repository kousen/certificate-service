# Script Draft: Sure, I'll Sign Your Ebook

## Working Title

Sure, I'll Sign Your Ebook

## Script

[COLD OPEN — Hat-Kenneth and No-Hat-Kenneth]

Hat-Kenneth: "Hey, love your books! Can you sign my copy?"

[Hat-Kenneth holds out a Kindle and a Sharpie]

No-Hat-Kenneth: [takes both, looks at the Kindle, looks at the Sharpie, looks at the camera]

[Beat]

---

Now, if somebody asks you to sign a physical book, that makes sense. You open the cover, write your name, maybe add a note, and everybody goes home happy.

But an ebook is a file.

There is no title page to sign. There is no pen involved. There is just a vague sense that somehow I, as the author, should be able to make this PDF or EPUB feel more personal.

And like many bad ideas in software, this one could have ended with me saying, "No, that's silly."

Instead, it turned into a Spring Boot application.

What I built is a service that generates a personalized certificate of ownership for one of my books. You enter your name, pick a title, and the app generates a PDF certificate with a decorative layout, my scanned signature, a digital signature, and a QR code that links to a verification page.

Which means that yes, I created a self-signed certificate signed with my own self-signed certificate.

That sentence alone was enough to justify the project.

Let me show you what it does.

[DEMO]

Here on the page, I type in a name, choose one of my books, and generate the certificate.

The result is a PDF that looks much more official than the situation deserves. Gold text. Decorative fonts. A background image. The person's name in cursive. "This certifies that so-and-so is the proud owner of" — and then the book title — "and has earned the author's eternal gratitude."

It's the most formal document I've ever produced for the least formal reason.

Now here's a detail I want to linger on, because I think it's genuinely interesting.

This certificate has two signatures. And they are doing completely different things.

[SHOW BOTH]

The first is the visible signature — my actual scanned handwriting, embedded in the PDF as an image. This is theater. It makes the certificate feel signed. It has zero cryptographic value. You could paste that image into anything.

The second is the digital signature. If you open the signature panel in a PDF reader, you'll see that the document is actually signed using a real X.509 certificate with SHA-512 and RSA 4096-bit keys.

That part is not theater. That's legitimate cryptographic integrity. If anyone modifies a single byte of this PDF, the signature breaks.

But — and here's where it gets funny — because I'm using a self-signed certificate, Adobe will immediately warn you that the signer is not trusted.

[SHOW THE WARNING]

And Adobe is correct. There is no certificate authority backing this up. The only entity vouching for the authenticity of this certificate... is me. The guy who made the certificate.

The signature is real. The trust model is just hilariously self-referential.

Now, the QR code. If you scan it, it opens a verification page hosted by the same application. The page shows the certificate's SHA-256 fingerprint and confirms the details.

And this is one of my favorite parts, because a QR code instantly makes anything look more official. Without it, this is a novelty PDF. With it, it starts to feel like a system. There's a verification endpoint. There's a hosted service. There's a little ecosystem around the joke.

[END DEMO]

But then, I decided it wasn't silly enough. I needed to move beyond "vibe coding" and implement some **technically real but contextually absurd** features.

So I added a "Nuclear-Grade Deep Verification" system.

When you click it, the app doesn't just check the database. It starts a full audit.

First, it syncs with a **Local Immutable Blockchain**. Yes, I implemented a functional, in-memory blockchain just to anchor these ebook certificates. Every certificate generation is a new block, with its own Merkle Root and "Proof of Existence." It's an immutable ledger that resets every time I restart the server.

Next, it performs **Biometric Stylometry Analysis**. The app uses PDFBox to extract real image metadata from the signature—dimensions, color space, and brightness levels—and runs them through a scoring algorithm. It's technically real data, used for a completely fake validation.

And finally, it adds **SHA-3 512-bit Quantum-Resistant Hashing**. It’s technically "Quantum-Resistant," and it’s also technically useless for verifying a PDF of a gag certificate.

But that's the point. The technical effort to build a blockchain, image analysis engine, and SHA-3 integration is entirely disproportionate to the problem. And in engineering, a disproportionate solution to a simple problem is the definition of a great joke.

So the joke works. But building it dragged me through APIs that were much more real than the problem deserved.

PDF generation, for instance. People talk about generating PDFs like you're just printing text into a document. But the second you want it to look good, everything gets fiddly.

PDFBox gives you a coordinate system and a content stream. You position every line of text manually. You load fonts — and if you're using custom fonts, you load them per document, because PDFBox 3.0 doesn't let you cache them across instances. You calculate string widths to center text. You layer a background image using a separate content stream in prepend mode so it renders behind the text.

And then you deploy to Heroku and discover that your decorative font doesn't load in that environment. So now you need a fallback path that rebuilds the entire document with standard Helvetica if the custom fonts fail.

The certificate still looks reasonable in fallback mode. But you've now written the same layout twice.

This is one of those classic software lessons: the last twenty percent of polish is where most of the effort lives.

Then there was the signing.

This is where the project went from "cute" to "surprisingly educational." I needed a keystore, a private key, a certificate chain, and BouncyCastle's CMS signed data generator to produce a PKCS#7 detached signature that PDFBox could embed.

The code is real cryptography in service of a joke. The `PdfSigner` class implements PDFBox's `SignatureInterface`, which means it plugs into the same signing pipeline that actual enterprise document systems use.

And that's the thing about Java's ecosystem. PDFBox and BouncyCastle are not elegant libraries. The APIs are verbose. The abstractions are thick. But they handle genuinely hard problems, and they handle them correctly. If what you need is "please take this weird file-processing and cryptographic task seriously," Java is an excellent choice.

Now, I should be honest about something. I had this idea years ago. And once I realized how much work it would actually take — digging through PDFBox's coordinate system, wiring up BouncyCastle's CMS signing pipeline, getting font embedding to behave — I didn't want to do it. The idea was funny, but the implementation looked like a week of joyless API archaeology.

So the project sat there, undone, for years.

What changed was AI coding tools. Specifically, I used Claude Code for most of the PDFBox and BouncyCastle work. And the reason is simple: if there's one thing AI tools are genuinely good at, it's automating the tedious and the annoying. Nobody wakes up excited to write coordinate math for centering text on a PDF page. Nobody has the BouncyCastle CMS signing API memorized.

If you've ever seen the Monty Python Ministry of Silly Walks sketch, there's a moment where John Cleese tells the man that his walk "isn't terribly silly." And the man replies, "Well, with government backing, I could make it a lot more silly."

That's basically what happened here. With AI backing, I could make this project a lot more silly.

The AI handled the API ceremony — the verbose, well-documented, fiddly code that's correct but not interesting to write by hand. I handled the decisions that actually mattered: what the certificate should look like, how signing and verification fit together, why the fonts broke on Heroku and what the fallback strategy should be.

That division of labor is what made a project I'd abandoned for years suddenly doable in a few sessions. And I think that's worth saying out loud, because a lot of developers have projects like this sitting in their "someday" list. The tedious middle is what kills fun ideas. If a tool can handle that middle, more fun ideas survive.

There's one more thing I want to point out. The app only signs my books. There's a custom Bean Validation annotation — `@ValidBookTitle` — and a hardcoded list of six titles. The record that holds the request uses `@NotBlank` and `@ValidBookTitle` together.

I wrote a custom constraint annotation and a validator class so that exactly six strings would be accepted by the system. That is the most overengineered whitelist in the history of input validation.

But also? It's the correct way to do it in Spring Boot. And that's kind of the point.

Which brings us to the ultimate sign that a side project has gone too far: the Analytics Dashboard.

[SHOW THE DASHBOARD]

I added an async analytics engine using Spring's `@Async` and Java 21 Virtual Threads. Every time someone generates or verifies a certificate, it's tracked. I have charts! I have metrics! I can tell you in real-time exactly how many people have certified their copy of "Mockito Made Clear" versus "Kotlin Cookbook".

Using Chart.js and Thymeleaf to build a dashboard for a joke is the exact moment you stop being a developer and start being a product manager for a gag.

And honestly, that's probably the real lesson here. Silly projects are useful because they give you permission to explore serious topics without the pressure of pretending you're building something important.

This app let me play with PDF generation, digital signatures, QR code workflows, custom validation, async event processing, and deployment — all in the service of a problem that absolutely did not need to exist.

That made it fun. And because it was fun, I kept going long enough to actually learn something.

Also — and I enjoy this — I wrote books to help developers get better at Java. And then I built a Java app to certify ownership of those books. The tool is the lesson.

So yes. If you buy one of my books, I can now provide a certificate proving that I have digitally acknowledged your ownership of a file.

Should this exist?

Probably not.

Am I glad I built it?

Absolutely.

If you want to see the code, I'll link it below. And if you have your own ridiculous side project, I'd love to hear about it.

Sometimes the best way to learn a platform is to use it for a problem nobody asked you to solve.

## Revision Notes

Changes from the GPT 5.4 draft:

1. **Title changed** from "I Built a Java App to Sign an Ebook. Yes, Really." to "Sure, I'll Sign Your Ebook" — shorter, punchier, better fit for the Tales from the Jar Side voice.

2. **Visual/digital signature distinction expanded** into a proper beat (~45 seconds) with the Adobe trust warning as a punchline. This is the most educational moment in the video and deserved more room.

3. **Specific code details added** throughout:
   - PDFBox per-document font loading and the Heroku fallback path
   - Content stream prepend mode for background images
   - `PdfSigner` implementing `SignatureInterface`
   - SHA-512/RSA-4096 key spec
   - The `@ValidBookTitle` annotation and six-title whitelist
   - PKCS#7 detached signatures via BouncyCastle CMS

4. **Claude Code acknowledgment added** after the PDFBox/BouncyCastle section. Framed as: these APIs are capable but tedious, AI tools handle the tedious part, you handle the design decisions and debugging. Honest without being self-deprecating.

5. **Meta-humor added**: "I wrote books to help developers get better at Java. And then I built a Java app to certify ownership of those books. The tool is the lesson."

5. **Analytics overengineering** mentioned briefly as evidence of scope creep, rather than explained in detail.

6. **Closing tightened** — removed the repeated "Java is good for unnecessary systems" formulation and ended on the cleaner "a problem nobody asked you to solve."

7. **Demo section structured** with [DEMO], [SHOW BOTH], [SHOW THE WARNING], [END DEMO] markers for shot planning.

8. **Overall length**: slightly longer than the original but the added material is all specific/visual — the generic "Java is capable" passages were trimmed to compensate. Should still land in 8-10 minutes at a natural speaking pace.
