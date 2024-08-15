package nyub.assert

import scala.quoted.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

class ExpectTest(
    val expectedContent: String,
    filePathAsString: String,
    val start: FilePosition,
    val end: FilePosition
) extends munit.Assertions:
    val filePath = Paths.get(filePathAsString)

    def run(actualContent: String) =
        val mode = System.getProperty("nyub.expect")
        if mode == "promote" then
            FilePosition.replaceBetween(
              filePath,
              s"${ExpectTest.tripleQuotes}${actualContent}${ExpectTest.tripleQuotes}",
              start,
              end
            )
        else if mode == "clear" then
            FilePosition.replaceBetween(
              filePath,
              s"${ExpectTest.tripleQuotes}${ExpectTest.tripleQuotes}",
              start,
              end
            )
        else assertEquals(expectedContent, actualContent)

object ExpectTest:
    extension (inline s: String)
        inline def expect(inline expected: String): Unit = ${
            expectImpl('s, 'expected)
        }

    def expectImpl(using
        Quotes
    )(s: Expr[String], expected: Expr[String]): Expr[Unit] =
        val et = expectTestImpl(expected)
        '{ $et.run($s) }

    def expectTestImpl(using Quotes)(s: Expr[String]): Expr[ExpectTest] =
        import quotes.reflect.*
        val tree: Tree = s.asTerm
        val sourceFile = tree.pos.sourceFile.getJPath
            .getOrElse(
              report.errorAndAbort(s"No source file for position ${tree.pos}")
            )
        '{
            ExpectTest(
              ${ s },
              ${ Expr(sourceFile.toString()) },
              FilePosition(
                ${ Expr(tree.pos.startLine) },
                ${ Expr(tree.pos.startColumn) }
              ),
              FilePosition(
                ${ Expr(tree.pos.endLine) },
                ${ Expr(tree.pos.endColumn) }
              )
            )
        }

    private val tripleQuotes = s"$"$"$""

case class FilePosition(val line: Int, val column: Int)
object FilePosition:
    def contentBetween(
        path: Path,
        start: FilePosition,
        end: FilePosition
    ): String =
        val lines = Files.readAllLines(path).subList(start.line, end.line + 1)
        if start.line == end.line then
            lines.get(0).substring(start.column, end.column)
        else
            val firstLineStripped = lines.get(0).substring(start.column)
            val lastLineStripped =
                lines.get(end.line - start.line).substring(0, end.column)
            lines.set(0, firstLineStripped)
            lines.set(end.line - start.line, lastLineStripped)
            lines
                .stream()
                .reduce("", (a, b) => if a != "" then a + "\n" + b else b)

    def replaceBetween(
        path: Path,
        replacement: String,
        start: FilePosition,
        end: FilePosition
    ): Unit =
        val lines = java.util.ArrayList(
          Files.readAllLines(path)
        ) // Wrap retuned list because it could be unmodifiable
        if start.line == end.line then
            val before = lines
                .get(start.line)
                .substring(0, start.column)
            val after = lines
                .get(start.line)
                .substring(end.column)
            lines.set(start.line, before + replacement + after)
        else
            val insertedContent =
                lines
                    .get(start.line)
                    .substring(0, start.column) + replacement + lines
                    .get(end.line)
                    .substring(end.column)
            lines.set(start.line, insertedContent)
            for _ <- start.line until end.line do lines.remove(start.line + 1)
        Files.write(path, lines): @annotation.nowarn("msg=discarded")
