package ftl.reports

import ftl.args.IArgs
import ftl.config.FtlConstants.indent
import ftl.json.MatrixMap
import ftl.reports.util.IReport
import ftl.reports.xml.model.JUnitTestResult
import ftl.util.asPrintableTable
import ftl.util.println
import java.io.StringWriter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**

Test Results. Always run.

Example:

59 / 100 (0.00%)
41 matrices failed

 **/
object MatrixResultsReport : IReport {
    override val extension = ".txt"

    private val percentFormat by lazy { DecimalFormat("#0.00", DecimalFormatSymbols(Locale.US)) }

    private fun generate(matrices: MatrixMap): String {
        val total = matrices.map.size
        // unfinished matrix will not be reported as failed since it's still running
        val success = matrices.map.values.count { it.failed().not() }
        val failed = total - success
        val successDouble: Double = success.toDouble() / total.toDouble() * 100.0
        val successPercent = percentFormat.format(successDouble)

        var outputData = "$success / $total ($successPercent%)"
        if (failed > 0) outputData += "$failed matrices failed"

        StringWriter().use { writer ->
            writer.println(reportName())
            writer.println("$indent$success / $total ($successPercent%)")
            if (failed > 0) {
                writer.println("$indent$failed matrices failed")
                writer.println()
            }
            if (matrices.map.isNotEmpty()) {
                writer.println("More details are available at [${matrices.map.values.first().webLinkWithoutExecutionDetails}]")
                writer.println(matrices.map.values.toList().asPrintableTable())
            }

            return writer.toString()
        }
    }

    override fun run(matrices: MatrixMap, result: JUnitTestResult?, printToStdout: Boolean, args: IArgs) {
        val output = generate(matrices)
        if (printToStdout) print(output)
        write(matrices, output, args)
    }
}
