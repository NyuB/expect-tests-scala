package nyub.assert

import ExpectTest.expect
class ExpectTestSuite extends munit.FunSuite:
    test("single line string"):
        "A".expect("""A""")

    test("multi-line string"):
        val content = """A
        B
    C
          D"""
        content.expect("""A
        B
    C
          D""")

end ExpectTestSuite
