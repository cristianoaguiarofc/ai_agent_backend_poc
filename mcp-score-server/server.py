#!/usr/bin/env python3
"""
Mock MCP server — Streamable HTTP transport
Expõe a tool: get_score(cpf: str) -> score: int
"""

import json
import random
import re
from http.server import BaseHTTPRequestHandler, HTTPServer

MCP_ENDPOINT = "/mcp"
PORT = 8090

TOOLS = [
    {
        "name": "get_score",
        "description": "Consulta o score de crédito de um CPF. Retorna um valor entre 0 e 1000.",
        "inputSchema": {
            "type": "object",
            "properties": {
                "cpf": {
                    "type": "string",
                    "description": "CPF do cliente (apenas dígitos ou formatado com pontos e traço)"
                }
            },
            "required": ["cpf"]
        }
    }
]


def get_score(cpf: str) -> int:
    digits = re.sub(r"\D", "", cpf)
    # Score determinístico baseado nos dígitos do CPF para resultados reproduzíveis
    seed = sum(int(d) * (i + 1) for i, d in enumerate(digits))
    return (seed * 37 + 113) % 1001


class McpHandler(BaseHTTPRequestHandler):

    def log_message(self, format, *args):
        print(f"[MCP] {self.address_string()} - {format % args}")

    def send_json(self, status: int, body: dict):
        data = json.dumps(body).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def read_body(self) -> dict:
        length = int(self.headers.get("Content-Length", 0))
        raw = self.rfile.read(length) if length else b"{}"
        return json.loads(raw)

    def do_POST(self):
        if self.path != MCP_ENDPOINT:
            self.send_json(404, {"error": "not found"})
            return

        body = self.read_body()
        method = body.get("method", "")
        req_id = body.get("id")

        if method == "initialize":
            self.send_json(200, {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": {
                    "protocolVersion": "2024-11-05",
                    "capabilities": {"tools": {}},
                    "serverInfo": {"name": "score-mock", "version": "1.0.0"}
                }
            })

        elif method == "tools/list":
            self.send_json(200, {
                "jsonrpc": "2.0",
                "id": req_id,
                "result": {"tools": TOOLS}
            })

        elif method == "tools/call":
            tool_name = body.get("params", {}).get("name")
            arguments = body.get("params", {}).get("arguments", {})

            if tool_name == "get_score":
                cpf = arguments.get("cpf", "")
                score = get_score(cpf)
                self.send_json(200, {
                    "jsonrpc": "2.0",
                    "id": req_id,
                    "result": {
                        "content": [
                            {
                                "type": "text",
                                "text": json.dumps({"cpf": cpf, "score": score})
                            }
                        ]
                    }
                })
            else:
                self.send_json(200, {
                    "jsonrpc": "2.0",
                    "id": req_id,
                    "error": {"code": -32601, "message": f"Tool '{tool_name}' not found"}
                })

        else:
            self.send_json(200, {
                "jsonrpc": "2.0",
                "id": req_id,
                "error": {"code": -32601, "message": f"Method '{method}' not found"}
            })


if __name__ == "__main__":
    server = HTTPServer(("localhost", PORT), McpHandler)
    print(f"Mock MCP score server rodando em http://localhost:{PORT}{MCP_ENDPOINT}")
    print("Tools disponíveis: get_score(cpf)")
    server.serve_forever()
